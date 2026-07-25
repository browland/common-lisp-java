package compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;

/**
 * Needs to be lazy due to the need to generate the assembly in the correct order regardless of what order the calls
 * are made.  So we store state about the asm we want to generate and we'll have one method which can dump the asm.
 * So we need a context to hold the state about what we're working on currently - call this AsmContext.
 */
public class AsmGenerator {
    private int stringLiteralIndex;
    private int formFunctionIndex;

    public void startForm(AsmContext context) {
        String asmFunctionName = "_fxn_" + formFunctionIndex++;
        Form form = new Form(asmFunctionName);
        context.startForm(form);
    }

    public void endForm(AsmContext context) {
        context.endForm();
    }

    public String generateStartTextRegion() {
        return """
                    .text
                    .global _main
                    """;
    }

    public String generate(AsmContext context) {
        String myAsm = generateStartTextRegion();

        myAsm += generateMainFunction(context);

        List<Form> forms = context.getFunctions();
        Form form = forms.getFirst();

        // TODO call either code for apply function or special form and keep them separate
        //      I also don't like that we recurse within the method; might be more intuitive to recurse here, e.g. by
        //      returning next node but appending to asm held in the context
        if (form.getOperator().getOperatorType() == OperatorType.FUNCTION) {
            myAsm += generateNextFormAsmForFunctionCall(form);
        }
        else if (form.getOperator().getOperatorType() == OperatorType.SPECIAL_FORM) {
            myAsm += generateNextFormAsmForSpecialForm(form);
        }
        myAsm += generateGlobals(context);

        return myAsm;
    }

    /**
     * The main function essentially just calls the function which implements the first top-level form.
     */
    private static String generateMainFunction(AsmContext context) {
        String myAsm = """
                .p2align 3
                _main:\n""";
        // Store frame pointer and link register
        myAsm += "  stp x29, x30, [sp, #-16]!\n";
        // Set our frame pointer to stack pointer
        myAsm += "  mov x29, sp\n";

        myAsm += "  bl " + context.getFunctions().getFirst().getAsmFunctionName() + "\n";

        // Restore function pointer and link register
        myAsm += "  ldp x29, x30, [x29]\n";

        // Free space from stack (16 bytes for the FP and LR)
        myAsm += "  add sp, sp, #16\n";

        // Return from _main
        myAsm += "  ret\n";
        return myAsm;
    }

    static String generateGlobals(AsmContext context) {
        // Anon. string literals, will be needed when we use string literals in code
        String myAsm = "";
        List<StringLiteral> stringLiterals = context.getStringLiterals();
        if (! stringLiterals.isEmpty()) {
            myAsm += ".cstring\n";
            for (StringLiteral stringLiteral : stringLiterals) {
                myAsm += stringLiteral.name() + ":\n";
                myAsm += "  .asciz \"" + stringLiteral.value() + "\"\n";
            }
        }

        // Static strings for things like messages
        myAsm += """
_error_msg:
  .asciz \"ERROR (incorrect value type)\\n\"""";
        return myAsm;
    }

    /**
     * Generates asm for next form encountered in the tree.  We generate the declaration, and if any other
     * forms are evaluated we make the calls to functions implementing them, and then we generate each of their
     * declarations by making a recursive call to this method.
     *
     * So far we evaluate operands supplied in the form of function calls but no other evaluation takes place.
     *
     * TODO consider we don't always want to recurse into inner forms.  E.g. if this form is (if ...) then we evaluate
     *      the first operand then evaluate one or the other of remaining operands and return its result.
     */
    private static String generateNextFormAsmForFunctionCall(Form thisForm) {
        String myAsm = "";
        myAsm += "\n";
        String name = thisForm.getAsmFunctionName();
        myAsm += ".p2align 3\n";
        myAsm += ".global " + name + "\n";
        myAsm += name + ":\n";

        // Store frame pointer and link register
        myAsm += "  stp x29, x30, [sp, #-16]!\n";
        // Set our frame pointer to stack pointer
        myAsm += "  mov x29, sp\n";

        // Needed only for function case; should extract out function handling
        List<Object> parts = thisForm.getRawParts();
        int numStackSlots = parts.size() - 1;
        int stackBytes = 8 * numStackSlots;

        Operator operator = thisForm.getOperator();
        // We always evaluate arguments for function calls, so reserve the stack needed
        // TODO should be eval args and copy to stack
        myAsm += copyArgsToStack(thisForm);
        myAsm += moveOperandsFromStackToRegisters(numStackSlots);

        if (OperatorName.ADD.equals(operator.getOperatorName())) {
            myAsm += new AddAsm().generate(thisForm);
        }


        // Restore function pointer and link register and free stack space we used to stash them
        myAsm += name + "_exit:\n";

        // Free space from stack (local variables for this function only)
        myAsm += "  add sp, sp, #" + stackBytes + "\n";
        // else for other types of values we'd recover the right amount of stack

        myAsm += "  ldp x29, x30, [sp], #16\n";
        myAsm += "  ret\n";

        // If Function parts exist, recurse to generate those too, concat'ing their output to myAsm
        for (Object part : parts) {
            if (part instanceof Form form) {
                if (form.getOperator().getOperatorType() == OperatorType.FUNCTION) {
                    myAsm += generateNextFormAsmForFunctionCall(form);
                }
                else if (form.getOperator().getOperatorType() == OperatorType.SPECIAL_FORM) {
                    myAsm += generateNextFormAsmForSpecialForm(form);
                }
            }
        }

        return myAsm;
    }

    private static String generateNextFormAsmForSpecialForm(Form form) {
        throw new UnsupportedOperationException("not implemented yet");
    }

    private static String copyArgsToStack(Form form) {
        // Reserve space on the stack for our operands which we need to figure out as we go
        // 0th part is the operator; we don't need that until the very end.
        // We also treat Function operands as just space on the stack for its return value and recurse to generate THAT function.
        // So we need to reserve len(parts)-1 slots on the stack.
        List<Object> parts = form.getRawParts();
        int numStackSlots = parts.size() - 1;

        // add 1 more slot if needed so we're 16 bytes aligned
        if (numStackSlots % 2 != 0) {
            numStackSlots++;
        }

        int stackBytes = 8 * numStackSlots;

        String myAsm = "  sub sp, sp, #" + stackBytes + "\n";

        // Move operands to stack
        for (int i = 1; i < parts.size(); i++) {
            // if an int then generate instructions for mov immediate value and ldr
            // if a function then generate instructions to call the function, then ldr x0 to appropriate stack pos
            Object part = parts.get(i);
            if (part instanceof Integer intPart) {
                long fixNum = createFixNum(intPart);
                myAsm += "  mov x0, #" + fixNum + "\n";
            } else if (part instanceof Form fxn) {
                myAsm += "  bl " + fxn.getAsmFunctionName() + "\n";
            }
            // so far this always works
            myAsm += "  str x0, [x29, #-" + (i * 8) + "]\n";  // i starts from 1 so the SP moves down in 8 byte chunks like 8, 16, 24 etc.
        }

        return myAsm;
    }

    private static long createFixNum(int intPart) {
        // Shift bits left by 3 places and tag the low 3 bits as 001 to indicate this is a fixnum.
        long fixNum = (long) intPart << 3;
        return fixNum | 0x1;
    }

    private static String moveOperandsFromStackToRegisters(int numStackSlots) {
        String myAsm = "";
        // for each operand (numStackSlots) move the appropriate operand from stack to next register

        // Stack offset relative to frame pointer (e.g. -8 is the highest 8-byte value, with -16 below it and so on).
        int stackOffset;
        String register;
        for (int i = 0; i < numStackSlots; i++) {
            stackOffset = (i + 1) * -8;
            register = "x" + i;
            myAsm += "  ldr " + register + ", [x29, #" + stackOffset + "]\n";
        }
        return myAsm;
    }

    public void pushInt(int i, AsmContext context) {
        context.pushInt(i);
    }

    public void withOperator(String operatorSymbol, AsmContext context) {
        context.withOperator(operatorSymbol);
    }

    StringLiteral addStringLiteral(String value, AsmContext context) {
        String name = "l_str_" + stringLiteralIndex++;
        StringLiteral stringLiteral = new StringLiteral(value, name);
        context.addStringLiteral(stringLiteral);
        return stringLiteral;
    }

    public void initMainFunction(BufferedWriter bw) throws IOException {
        bw.write(
                """
        .text
        .global _main
        _main:
        .p2align 3
          stp x29, x30, [sp, #-16]!  ;; allocate stack and store frame pointer and link register
          mov x29, sp                ;; set frame pointer
          bl _init                   ;; init symbol table etc
        """);
    }

    public void printResultAndCleanUpMainFunction(BufferedWriter bw) throws IOException {
        bw.write("""
  bl _printResult
  ldp x29, x30, [x29]
  add sp, sp, #16
  ret
""");
    }

    public void reserveSpaceOnStack(int stackBytes, BufferedWriter bw) throws IOException {
        bw.write("""
    sub sp, sp, #%d           ;; reserve space for operands
  """.formatted(stackBytes));
    }

    /**
     * pos starts from 0 and is used to determine the stack offset
     */
    public void pushFixNumToStack(int pos, long fixNum, BufferedWriter bw) throws IOException {
        bw.write("""
    mov x0, #%d          ;; move operand (fixnum) to x0
    str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
  """.formatted(fixNum, pos*8));
    }

    public void storeResultToStack(int operandNum, BufferedWriter bw) throws IOException {
        bw.write("""
  str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
""".formatted(operandNum*8));
    }

    /**
     * operandNum starts from 0
     */
    public void loadOperandFromStackIntoRegister(int operandNum, BufferedWriter bw) throws IOException {
        bw.write("""
      ldr x%d, [sp, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(operandNum, operandNum*8));
    }

    public void callFunction(BufferedWriter bw, OperatorName operatorName)  throws  IOException {
        bw.write("""
      bl %s                ;; call operator; this leaves the result in x0 for our caller
    """.formatted(operatorName.getAsmName()));
    }

    public void freeSpaceOnStack(int stackBytes, BufferedWriter bw) throws IOException {
        bw.write("""
                  add sp, sp, #%d
                """.formatted(stackBytes));
    }

    public void generateSymbolLookup(String value, BufferedWriter bw) throws IOException {
        // todo very stub code for now to force lookup of t
        //      For ech defined symbol we'll have a global with a well-defined name.  So far we're calling them t_symbol_ptr and nil_symbol_ptr and so on
        //      The built-in symbols will be defined in runtime.c and any user-defined ones will be added to the .cstring section of generated asm.
        //      E.g. for a user-defined symbol myvar, we'd generate a 'quad' data with name myvar_symbol_ptr, we'd strdup the char* symbol name so it's on the
        //      heap, we'd then store that pointer with the appropriate tagged bits in myvar_symbol_ptr.
        //      So for lookup, we can reference the pointer name by its well-defined name, get its value, check its type, remove the tag bits, dereference it, 
        //      and that's the symbol.  Its symbol table entry would use the same tagged pointer.

        // Generate the well-defined name of the runtime symbol holding the tagged pointer
        String symPointerName = "_" + value + "_symbol_ptr";
        bw.write("""
  adrp x0, %s@PAGE                     ; get page of tagged symbol pointer variable
  add x0, x0, %s@PAGEOFF               ; add offset of tagged symbol pointer variable so x0 contains its address
  ldr x0, [x0]                         ; dereference pointer so we return (in x0) the actual tagged (symbol) pointer
  bl _evaluate_symbol                  ; look up value of this symbol in variable namespace
""".formatted(symPointerName, symPointerName));
    }

    public void generateTypeCheckForSymbol(BufferedWriter bw) throws IOException {
        bw.write("""
                bl _typecheck_symbol
                """);
    }

    public void generateCheckForT(BufferedWriter bw) throws IOException {
        bw.write("""
                bl _is_t
                """);
    }

    public void generateJumpInstructionForIf(BufferedWriter bw, String falseLabel) throws IOException {
        bw.write("""
                cbnz x0, %s
                """.formatted(falseLabel));
    }

    public void generateLabel(BufferedWriter bw, String label) throws IOException {
        bw.write("""
                .global %s
                %s:
                """.formatted(label, label));


    }
}
