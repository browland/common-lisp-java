package compiler;

import java.util.List;

public class AddAsm implements FormAsm {
    @Override
    public String generate(Form form) {
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
            if (part instanceof Integer) {
                myAsm += "mov x0, #" + part + "\n";
            } else if (part instanceof Form fxn) {
                myAsm += "bl " + fxn.getAsmFunctionName() + "\n";
            }
            // so far this always works
            myAsm += "str x0, [x29, #-" + (i * 8) + "]\n";  // i starts from 1 so the SP moves down in 8 byte chunks like 8, 16, 24 etc.
        }

        // call the operator - hardcode for now as this is getting too dicey
        if (parts.getFirst() instanceof Operator op) {  // all we have for now
            myAsm += moveOperandsFromStackToRegisters(numStackSlots);
            myAsm += "add x0, x0, x1\n";
        }

        // Free space from stack (local variables for this function only)
        myAsm += "add sp, sp, #" + stackBytes + "\n";
        return myAsm;
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
}
