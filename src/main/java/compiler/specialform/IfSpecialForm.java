package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.Node;
import syntaxtree.RList;
import treewalker.TreeWalker;

import java.io.BufferedWriter;
import java.io.IOException;

public class IfSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, BufferedWriter bw, AsmGenerator asmGenerator) throws IOException {
        // generate asm to evaluate first operand; this involves recursing back into TreeWalker
        // if we eval the first form then we'll end up with the result in x0.
        Node conditionNode = rlist.get(1);
        treeWalker.walkTree(conditionNode);

        asmGenerator.reserveSpaceOnStack(16, bw);
        asmGenerator.storeResultToStack(0, bw);

        // we now have the result of evaluating the condition in x0.  It should be either t or nil.  Generate asm to
        // type-check and then generate code for each branch, each with a label and jump to the right one.
        asmGenerator.generateTypeCheckForSymbol(bw);

        // return result (t which is our condition) back off stack again
        asmGenerator.loadOperandFromStackIntoRegister(0, bw);
        asmGenerator.freeSpaceOnStack(16, bw);
    }
}
