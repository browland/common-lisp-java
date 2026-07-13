package compiler;

import java.util.List;

/**
 * Needs to be lazy due to the need to generate the assembly in the correct order regardless of what order the calls
 * are made.  So we store state about the asm we want to generate and we'll have one method which can dump the asm.
 * So we need a context to hold the state about what we're working on currently - call this AsmContext.
 */
public class AsmGenerator {
    private int stringLiteralIndex;
    private int functionIndex;

    StringLiteral addStringLiteral(String value, AsmContext context) {
        String name = "l_str_" + stringLiteralIndex++;
        StringLiteral stringLiteral = new StringLiteral(value, name);
        context.addStringLiteral(stringLiteral);
        return stringLiteral;
    }

    public void startFunction(AsmContext context) {
        String name = "_fxn_" + functionIndex++;
        Function function = new Function(name);
        context.startFunction(function);
    }

    public void endFunction(AsmContext context) {
        context.endFunction();
    }

    public String generate(AsmContext context) {
        String myAsm = """
                    .text
                    .global _main
                    .p2align 3
                _main:\n""";
        // Store frame pointer and link register
        myAsm += "stp x29, x30, [sp, #-16]!\n";
        // Set our frame pointer to stack pointer
        myAsm += "mov x29, sp\n";

        myAsm += "bl " + context.getFunctions().getFirst().getName() + "\n";

        // Restore function pointer and link register
        myAsm += "ldp x29, x30, [x29]\n";

        // Free space from stack (16 bytes for the FP and LR)
        myAsm += "add sp, sp, #16\n";

        // Return from _main
        myAsm += "ret\n";

        // Functions ...
        List<Function> functions = context.getFunctions();

        // TODO fixed a bug here where we were looping over the functions here; we walk through them internally.
        Function function = functions.getFirst();
        myAsm += generateFunction(function);

        // String literals ...
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

    private static String generateFunction(Function function) {
        String myAsm = "";
        myAsm += "\n";
        String name = function.getName();
        myAsm += ".p2align 3\n";
        myAsm += ".global " + name + "\n";
        myAsm += name + ":\n";

        // Store frame pointer and link register
        myAsm += "stp x29, x30, [sp, #-16]!\n";
        // Set our frame pointer to stack pointer
        myAsm += "mov x29, sp\n";

        // Reserve space on the stack for our operands which we need to figure out as we go
        List<Object> parts = function.getParts();
        // 0th part is the operator; we don't need that until the very end.
        // We also treat Function operands as just space on the stack for its return value and recurse to generate THAT function.
        // So we need to reserve len(parts)-1 slots on the stack.
        int numStackSlots = parts.size()-1;

        // add 1 more slot if needed so we're 16 bytes aligned
        if (numStackSlots % 2 != 0) {
            numStackSlots++;
        }

        int stackBytes = 8*numStackSlots;

        myAsm += "sub sp, sp, #" + stackBytes + "\n";

        // Move operands to stack
        for (int i=1; i<parts.size(); i++) {
            // if an int then generate instructions for mov immediate value and ldr
            // if a function then generate instructions to call the function, then ldr x0 to appropriate stack pos
            Object part = parts.get(i);
            if (part instanceof Integer) {
                myAsm += "mov x0, #" + part + "\n";
            }
            else if (part instanceof Function fxn) {
                myAsm += "bl " + fxn.getName() + "\n";
            }
            // so far this always works
            myAsm += "str x0, [x29, #-" + (i*8) + "]\n";  // i starts from 1 so the SP moves down in 8 byte chunks like 8, 16, 24 etc.
        }

        // call the operator - hardcode for now as this is getting too dicey
        if (parts.getFirst() instanceof Operator op) {  // all we have for now
            myAsm += moveOperandsFromStackToRegisters(numStackSlots);
            myAsm += executeInstruction(op);
        }

        // Restore function pointer and link register
        myAsm += "ldp x29, x30, [x29]\n";

        // Free space from stack, including the 16 bytes for the FP and LR
        myAsm += "add sp, sp, #" + (16 + stackBytes) + "\n";

        myAsm += "ret\n";

        // If Function parts exist, recurse to generate those too, concat'ing their output to myAsm
        for (Object part : parts) {
            if (part instanceof Function fxn) {
                myAsm += generateFunction(fxn);
            }
        }

        return myAsm;
    }

    private static String moveOperandsFromStackToRegisters(int numStackSlots) {
        String myAsm = "";
        // for each operand (numStackSlots) move the appropriate operand from stack to next register

        // Stack offset relative to frame pointer (e.g. -8 is the highest 8-byte value, with -16 below it and so on).
        int stackOffset;
        String register;
        for (int i = 0; i<numStackSlots; i++) {
            stackOffset = (i+1) * -8;
            register = "x" + i;
            myAsm += "ldr " + register + ", [x29, #" + stackOffset + "]\n";
        }
        return myAsm;
    }

    private static String executeInstruction(Operator op) {
        String myAsm = "";
        if ("+".equals(op.getOperator())) {
            myAsm += "add x0, x0, x1\n";
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
