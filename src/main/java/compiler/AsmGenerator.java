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

    StringLiteral addStringLiteral(String value, AsmContext context) {
        String name = "l_str_" + stringLiteralIndex++;
        StringLiteral stringLiteral = new StringLiteral(value, name);
        context.addStringLiteral(stringLiteral);
        return stringLiteral;
    }

    public void startForm(AsmContext context) {
        String asmFunctionName = "_fxn_" + formFunctionIndex++;
        Form form = new Form(asmFunctionName);
        context.startForm(form);
    }

    public void endForm(AsmContext context) {
        context.endForm();
    }

    public String generateStartTextRegion(AsmContext context) {
        return """
                    .text
                    .global _main
                    """;
    }

    public String generate(AsmContext context) {
        String myAsm = generateStartTextRegion(context);

        // The main function essentially calls the function which implements the top-level form
        myAsm += generateMainFunction(context);

        // Functions ...
        List<Form> forms = context.getFunctions();

        Form form = forms.getFirst();
        myAsm += generateNextFormFunction(form);

        myAsm += generateGlobals(context);

        return myAsm;
    }

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
        // String literals ...
        String myAsm = "";
        List<StringLiteral> stringLiterals = context.getStringLiterals();
        if (! stringLiterals.isEmpty()) {
            myAsm += ".cstring\n";
            for (StringLiteral stringLiteral : stringLiterals) {
                myAsm += stringLiteral.name() + ":\n";
                myAsm += "   .asciz \"" + stringLiteral.value() + "\"\n";
            }
        }
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
    private static String generateNextFormFunction(Form nextForm) {
        String myAsm = "";
        myAsm += "\n";
        String name = nextForm.getAsmFunctionName();
        myAsm += ".p2align 3\n";
        myAsm += ".global " + name + "\n";
        myAsm += name + ":\n";

        // Store frame pointer and link register
        myAsm += "stp x29, x30, [sp, #-16]!\n";
        // Set our frame pointer to stack pointer
        myAsm += "mov x29, sp\n";

        // TODO look at nextForm.parts.getFirst() - should extract out the op
        myAsm += new AddAsm().generate(nextForm);

        // Restore function pointer and link register and free stack space we used to stash them
        myAsm += "ldp x29, x30, [sp], #16\n";

        myAsm += "ret\n";

        // If Function parts exist, recurse to generate those too, concat'ing their output to myAsm
        List<Object> parts2 = nextForm.getRawParts();
        for (Object part : parts2) {
            if (part instanceof Form fxn) {
                myAsm += generateNextFormFunction(fxn);
            }
        }

        return myAsm;
    }


    public void pushInt(int i, AsmContext context) {
        context.pushInt(i);
    }

    public void withOperator(String op, AsmContext context) {
        context.withOperator(op);

    }
}
