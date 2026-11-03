package compiler.specialform;

import compiler.Namespace;
import compiler.treewalker.CompilerBackend;
import compiler.treewalker.SymbolAtom;
import compiler.treewalker.TreeWalker;
import compiler.treewalker.TypedAtom;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

public class ReturnFromSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        // Get name of block to return from.  This looks like a symbol but is definitely not evaluated, so e.g. the block name could be nil and
        // it's considered as just a name in block/return-from.
        Node nameNode = rlist.nodes().get(1);
        Atom nameAtom = nameNode instanceof Atom ? (Atom)nameNode : null;
        if (nameAtom == null) {
            throw new IllegalArgumentException("return-from: named block is not an atom in: " + rlist);
        }

        TypedAtom<?> typedAtom = TypedAtom.fromAtom(nameAtom);
        String returnFromName = null;
        if (typedAtom instanceof SymbolAtom symbolAtom) {
            returnFromName = symbolAtom.getValue();
        }

        if (returnFromName == null) {
            throw new IllegalArgumentException("No name supplied for return-from in: " + rlist);
        }

        // Find optional return value
        if (rlist.nodes().size() == 2) {
            backend.handleSymbolOperand("nil", Namespace.VARIABLE);
        }
        else {
            // Need to evaluate the return value which could be any form.
            Node returnValueNode = rlist.nodes().get(2);
            treeWalker.walkTree(returnValueNode);
        }

        String endLabel = "block_" + returnFromName + "_end";
        backend.jumpToLabel(endLabel);
    }
}
