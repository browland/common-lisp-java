package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.Node;
import syntaxtree.RList;
import treewalker.TreeWalker;

import java.io.BufferedWriter;
import java.io.IOException;

public class IfSpecialForm implements SpecialForm {
    private static long BRANCH_COUNTER = 0L;

    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, BufferedWriter bw, AsmGenerator asmGenerator) throws IOException {
        // generate asm to evaluate first operand; this involves recursing back into TreeWalker
        // if we eval the first form then we'll end up with the result in x0.
        Node conditionNode = rlist.get(1);
        Node trueNode = rlist.get(2);
        Node falseNode = rlist.get(3);

        treeWalker.walkTree(conditionNode);

        asmGenerator.reserveSpaceOnStack(16, bw);
        asmGenerator.storeResultToStack(0, bw);

        // we now have the result of evaluating the condition in x0.  It should be either t or nil.  Generate asm to
        // type-check and then generate code for each branch, each with a label and jump to the right one.
        asmGenerator.generateTypeCheckForSymbol(bw);

        // return result (t which is our condition) back off stack again
        asmGenerator.loadOperandFromStackIntoRegister(0, bw);

        // check for t, will set x0 to 0 if result was true
        asmGenerator.generateCheckForT(bw);

        // generate code to inspect result and depending on whether it's t or not then evaluate the appropriate form
        long thisBranchCounter = BRANCH_COUNTER++;
        String falseLabel = "if_false_" + thisBranchCounter;
        String trueLabel = "if_true" + thisBranchCounter;
        String exitLabel = "if_exit" + thisBranchCounter;

        // Jump to false if required; we fall through to code for true branch
        asmGenerator.generateJumpInstructionForNonZeroReturnValue(bw, falseLabel);

        // generate code for the two branches, each with a label; true first.  We also need an exit label.
        asmGenerator.generateLabel(bw, trueLabel);
        treeWalker.walkTree(trueNode);
        asmGenerator.generateUnconditionalJump(bw, exitLabel);  // jump to exit

        // generate false branch code
        asmGenerator.generateLabel(bw, falseLabel);
        treeWalker.walkTree(falseNode);
        asmGenerator.generateUnconditionalJump(bw, exitLabel);  // jump to exit

        asmGenerator.generateLabel(bw, exitLabel);

        asmGenerator.freeSpaceOnStack(16, bw);
    }
}
