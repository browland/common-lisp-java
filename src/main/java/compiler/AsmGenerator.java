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

    public void storeResultToStack(int pos, BufferedWriter bw) throws IOException {
        bw.write("""
  str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
""".formatted(pos*8));
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

    public void generateAddAsm(BufferedWriter bw) throws IOException {
        bw.write("""

.global _add
_add:
  mov x2, #0x7    ; bit mask to select out the low 3 bits only
  and x3, x0, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, _no_match
            
  and x3, x1, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, _no_match
_match:
  lsr x0, x0, #3  ; logical shift right x0 to remove type tag bits
  lsr x1, x1, #3  ; logical shift right x1 to remove type tag bits
  add x0, x0, x1
                
  lsl x0, x0, #3
  orr x0, x0, #0x1
  ret
_no_match:  ;; no match
  adrp x0, _error_msg@PAGE
  add x0, x0, _error_msg@PAGEOFF
  bl _printf
  bl _exit
""");
    }

    // MacOS has a weird thing where making a call to printf requires the numeric argument to be on the stack at [sp]
    // while the string pointer is in x0 as usual.
    // More generally, any functions accepting variadic arguments (printf is a good example) require the "optional"
    // args to be on the stack.
    public void generatePrintResultAsm(BufferedWriter bw) throws IOException {
        bw.write("""
.global _printResult
_printResult:
  mov x2, #0x7    ; bit mask to select out the low 3 bits only
  and x3, x0, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, _pR_no_match
_pR_match:
  stp x29, x30, [sp, #-16]!  ;; allocate stack and store frame pointer and link register
  lsr x0, x0, #3  ; logical shift right x0 to remove type tag bits
  sub sp, sp, #16
  str x0, [sp]
  adrp x0, _printed_result@PAGE
  add x0, x0, _printed_result@PAGEOFF
  bl _printf
  add sp, sp, #16
  ldp x29, x30, [sp], #16
  ret
  
_pR_no_match:  ;; no match
  adrp x0, _error_msg@PAGE
  add x0, x0, _error_msg@PAGEOFF
  bl _printf
  bl _exit
""");
    }

    public void generateGlobals(BufferedWriter bw) throws IOException {
        // Static strings for things like messages
        bw.write("""

.cstring:
_error_msg:
  .asciz \"ERROR (incorrect value type)\\n\"
_printed_result:
  .asciz \"result: %d\\n\"
_t:
  .asciz \"t\\n\"
  
.data
.p2align 3
; Number of entries in the symbol table
sym_size:
  .quad 0
; Capacity of symbol table (can grow at runtime)
sym_capacity:
  .quad 100
; Pointer to symbol table
sym_ptr:
  .quad 0
""");
    }

    public void setUpSymbolTable(BufferedWriter bw) throws IOException {
        bw.write("""
  ; load ptrs to var to hold sym table ptr, and to capacity
  adrp x0, sym_capacity@PAGE
  add x0, x0, sym_capacity@PAGEOFF
  
  ; Allocate symbol table
  ; 3 slots in table for each symbol (1. ptr to or value of symbol, 2. ptr to variable, 3. ptr to function)
  ; 8 bytes for each slot in symbol table
  ; so multiply sym table capacity by 24
  ldr x0, [x0]   ; load capacity num into x0
  mov x2, #24    ; set up bytes multiplier
  mul x0, x0, x2 ; set total bytes for sym table into x0
  bl _malloc     ; allocate symbol table
  adrp x1, sym_ptr@PAGE
  add x1, x1, sym_ptr@PAGEOFF
  str x0, [x1]   ; store ptr to sym table in its slot (sym_ptr variable)
  
  ;;;;;;;;;;;;
  ; Set up "t"
  ;;;;;;;;;;;;
  ; Our symbols are tagged with low 3 bits 100
  ; Remember symbols themselves are null-terminated C strings, but here we just manage pointers, it will matter though when we come to searching the table.
  ; load ptr to "t"
  adrp x1, _t@PAGE
  add x1, x1, _t@PAGEOFF
  adrp x2, sym_ptr@PAGE
  add x2, x2, sym_ptr@PAGEOFF
  ; write "t" symbol ptr to first slot in sym table
  str x1, [x2]
  ; make "t" self-evaluating; its variable slot will hold itself
  str x1, [x2, #8]
  ; we leave the function slot empty for now
  
                
""");
    }
}
