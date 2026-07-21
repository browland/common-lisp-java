package compiler.specialform;

import syntaxtree.Node;
import syntaxtree.RList;
import treewalker.TreeWalker;

import java.io.IOException;

public class IfSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker) throws IOException {
        // generate asm to evaluate first operand; this involves recursing back into TreeWalker
        // if we eval the first form then we'll end up with the result in x0.
        Node conditionNode = rlist.get(1);
        treeWalker.walkTree(conditionNode);
    }
}
