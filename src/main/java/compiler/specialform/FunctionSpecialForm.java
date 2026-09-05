package compiler.specialform;

import compiler.Namespace;
import compiler.treewalker.CompilerBackend;
import compiler.treewalker.TreeWalker;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

/*
 * `function` needs to be a special form otherwise we'd evaluate the symbol and get its value slot rather than its
 * function slot.  We just return the function object from the function slot associated with this symbol.
 */
public class FunctionSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        Node functionSymbolNode = rlist.get(1);
        Atom functionAtom = Atom.expectAtom(functionSymbolNode);

        // Look up the symbol, get its function slot and leave that in x0
        backend.handleSymbolOperand(functionAtom.value(), Namespace.FUNCTION);
    }
}
