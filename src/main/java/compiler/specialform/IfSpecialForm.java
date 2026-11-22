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

        // we now have the result of evaluating the condition in x0.  It should be either t or nil.  Generate asm to
        // generate code for each branch, each with a label and jump to the right one.

        // check for nil, will set x0 to 0 if result was nil
        backend.resultWasNil();

        long thisBranchCounter = BRANCH_COUNTER++;

        String nilLabel = "if_nil" + thisBranchCounter;
        String exitLabel = "if_exit" + thisBranchCounter;
        String falseLabel = "if_false_" + thisBranchCounter;

        if (falseNode == null) {
            backend.jumpToLabelIfResultZero(nilLabel);
        }
        else {
            backend.jumpToLabelIfResultZero(falseLabel);
        }

        // generate code to jump to true if nil check was false
        String trueLabel = "if_true" + thisBranchCounter;

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
    }
}
