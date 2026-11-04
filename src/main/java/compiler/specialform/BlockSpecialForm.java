package compiler.specialform;

import compiler.treewalker.*;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

public class BlockSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        // Get name of block to return from.  This looks like a symbol but is definitely not evaluated, so e.g. the block name could be nil and
        // it's considered as just a name in block/return-from.
        // TODO track block names in compiler backend like we do with tags - then validate lexical scope in return-from
        Node nameNode = rlist.nodes().get(1);
        Atom nameAtom = nameNode instanceof Atom ? (Atom)nameNode : null;
        if (nameAtom == null) {
            throw new IllegalArgumentException("First node in block was not an atom: " + rlist);
        }

        TypedAtom<?> typedAtom = TypedAtom.fromAtom(nameAtom);
        String blockName = null;
        if (typedAtom instanceof SymbolAtom symbolAtom) {
            blockName = symbolAtom.getValue();
        }

        if (blockName == null) {
            throw new IllegalArgumentException("No name supplied for block in: " + rlist);
        }

        String startLabel = "block_" + blockName + "_start";
        String endLabel = "block_" + blockName + "_end";
        backend.generateLabel(startLabel);

        for (Node curNode : rlist.nodes().subList(2, rlist.nodes().size())) {
            treeWalker.walkTree(curNode);
        }

        backend.generateLabel(endLabel);
    }
}
