package compiler;

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

        myAsm += generateNextFormAsmForFunctionCall(form);
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
        myAsm += "stp x29, x30, [sp, #-16]!\n";
        // Set our frame pointer to stack pointer
        myAsm += "mov x29, sp\n";

        myAsm += "bl " + context.getFunctions().getFirst().getAsmFunctionName() + "\n";

        // Restore function pointer and link register
        myAsm += "ldp x29, x30, [x29]\n";

        // Free space from stack (16 bytes for the FP and LR)
        myAsm += "add sp, sp, #16\n";

        // Return from _main
        myAsm += "ret\n";
        return myAsm;
    }

    private static String generateGlobals(AsmContext context) {
        // Anon. string literals, will be needed when we use string literals in code
        String myAsm = "";
        List<StringLiteral> stringLiterals = context.getStringLiterals();
        if (! stringLiterals.isEmpty()) {
            myAsm += ".cstring\n";
            for (StringLiteral stringLiteral : stringLiterals) {
                myAsm += stringLiteral.name() + ":\n";
                myAsm += "   .asciz \"" + stringLiteral.value() + "\"\n";
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
        myAsm += "stp x29, x30, [sp, #-16]!\n";
        // Set our frame pointer to stack pointer
        myAsm += "mov x29, sp\n";

        // Needed only for function case; should extract out function handling
        List<Object> parts = thisForm.getRawParts();
        int numStackSlots = parts.size() - 1;
        int stackBytes = 8 * numStackSlots;

        Operator operator = thisForm.getOperator();
        if (operator.getOperatorType() == OperatorType.FUNCTION) {
            // We always evaluate arguments for function calls, so reserve the stack needed
            myAsm += copyArgsToStack(thisForm);
            myAsm += moveOperandsFromStackToRegisters(numStackSlots);
        }

        if (OperatorName.ADD.equals(operator.getOperator())) {
            myAsm += new AddAsm().generate(thisForm);
        }

        if (operator.getOperatorType() == OperatorType.FUNCTION) {
            // Free space from stack (local variables for this function only)
            myAsm += "add sp, sp, #" + stackBytes + "\n";
        }

        // Restore function pointer and link register and free stack space we used to stash them
        myAsm += "ldp x29, x30, [sp], #16\n";

        myAsm += "ret\n";

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

        String myAsm = "sub sp, sp, #" + stackBytes + "\n";

        // Move operands to stack
        for (int i = 1; i < parts.size(); i++) {
            // if an int then generate instructions for mov immediate value and ldr
            // if a function then generate instructions to call the function, then ldr x0 to appropriate stack pos
            Object part = parts.get(i);
            if (part instanceof Integer intPart) {
                long fixNum = createFixNum(intPart);
                myAsm += "mov x0, #" + fixNum + "\n";
            } else if (part instanceof Form fxn) {
                myAsm += "bl " + fxn.getAsmFunctionName() + "\n";
            }
            // so far this always works
            myAsm += "str x0, [x29, #-" + (i * 8) + "]\n";  // i starts from 1 so the SP moves down in 8 byte chunks like 8, 16, 24 etc.
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
            myAsm += "ldr " + register + ", [x29, #" + stackOffset + "]\n";
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
}
