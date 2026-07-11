package treewalker;

import java.util.List;

public class ApplyFormFunctionGenerator {
    private static int counter;

    public static String generateApplyForm(List<ProcessedNode> processedNodes) {
        String functionName = "applyForm_" + counter++;

        // TODO emit instructions for apply function, using processedNodes.  These may be TypedAtom instances or
        //      could themselves be ProcessedForm instances with generated apply function which we need to call
        // TODO where/how do we emit instructions?
        // TODO need generator hierarchy for stuff which generates assembly
        // Steps: TODO - only works for functions not special forms
        // 1. Save FP, LR to stack
        // 2. Reserve space on stack for each arg, this is size of processedNodes minus 1 (for the operator), 8 bytes
        //    for each for now.
        // 3. Write each value to stack in order (in descending address as we go up through list).
        //    Any time we encounter a ProcessedForm we insert a call to the named apply form function, then put result
        //    in appropriate place on stack.
        // 4. Once all args on stack, move them into registers ready for the call
        // 5. Now insert call to the actual operator in position 0
        // 6. Move SP back up to free space from stack
        // 7. Restore FP, LR from stack
        // 8. Insert return instruction

        return functionName;
    }
}
