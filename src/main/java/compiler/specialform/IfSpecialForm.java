package compiler.specialform;

import compiler.Namespace;
import compiler.treewalker.CompilerBackend;
import compiler.treewalker.TreeWalker;
import syntaxtree.Node;
import syntaxtree.RList;

public class IfSpecialForm implements SpecialForm {
    private static long BRANCH_COUNTER = 0L;

    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        // generate asm to evaluate first operand; this involves recursing back into TreeWalker
        // if we eval the first form then we'll end up with the result in x0.
        Node conditionNode = rlist.get(1);
        Node trueNode = rlist.get(2);

        // The 'else' node is optional; if omitted and the 'true' branch is false then no action is taken and nil is returned.
        Node falseNode = rlist.size() > 3 ? rlist.get(3) : null;

        treeWalker.walkTree(conditionNode);

        backend.reserveStackForVariables(2);  // 2 variables to get 16 byte alignment
        backend.recordStackAllocation(16);
        backend.storeResultToStack(0);

        // we now have the result of evaluating the condition in x0.  It should be either t or nil.  Generate asm to
        // type-check and then generate code for each branch, each with a label and jump to the right one.
        backend.typeCheckResultIsSymbol();

        // return result (t which is our condition) back off stack again
        backend.loadVariableFromStackIntoRegister(0, 0);

        // check for t, will set x0 to 0 if result was true
        backend.resultWasT();

        long thisBranchCounter = BRANCH_COUNTER++;

        String nilLabel = "if_nil" + thisBranchCounter;
        String exitLabel = "if_exit" + thisBranchCounter;
        if (falseNode == null) {
            backend.jumpToLabelIfResultNonZero(nilLabel);
        }

        // generate code to inspect result and depending on whether it's t or not then evaluate the appropriate form
        String trueLabel = "if_true" + thisBranchCounter;

        // Jump to false if required; we fall through to code for true branch
        String falseLabel = null;
        if (falseNode != null) {
            falseLabel = "if_false_" + thisBranchCounter;
            backend.jumpToLabelIfResultNonZero(falseLabel);
        }

        // generate code for the two branches, each with a label; true first.  We also need an exit label.
        backend.generateLabel(trueLabel);
        treeWalker.walkTree(trueNode);
        backend.jumpToLabel(exitLabel);  // jump to exit

        // generate false branch code
        if (falseNode != null) {
            backend.generateLabel(falseLabel);
            treeWalker.walkTree(falseNode);
            backend.jumpToLabel(exitLabel);  // jump to exit
        }
        else {
            // generate code for the nil branch (condition false and no else clause)
            backend.generateLabel(nilLabel);
            backend.handleSymbolOperand("nil", Namespace.VARIABLE);
            backend.jumpToLabel(exitLabel);  // jump to exit
        }

        backend.generateLabel(exitLabel);

        backend.freeStackForVariables(2);
        backend.recordStackAllocation(-16);
    }
}
