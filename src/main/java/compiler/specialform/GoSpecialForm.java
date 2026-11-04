package compiler.specialform;

import compiler.treewalker.CompilerBackend;
import compiler.treewalker.SymbolAtom;
import compiler.treewalker.TreeWalker;
import compiler.treewalker.TypedAtom;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

public class GoSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        // Get name of tag to go to.
        Node nameNode = rlist.nodes().get(1);
        Atom nameAtom = nameNode instanceof Atom ? (Atom)nameNode : null;
        if (nameAtom == null) {
            throw new IllegalArgumentException("First node after go was not an atom: " + rlist);
        }

        TypedAtom<?> typedAtom = TypedAtom.fromAtom(nameAtom);
        String tagName = null;
        if (typedAtom instanceof SymbolAtom symbolAtom) {
            tagName = symbolAtom.getValue();
        }

        if (tagName == null) {
            throw new IllegalArgumentException("No tag supplied in go: " + rlist);
        }

        String asmLabel = backend.getAsmLabelForTag(tagName).orElseThrow(() -> new RuntimeException("Cannot find tag in current scope: " + rlist));
        backend.jumpToLabel(asmLabel);
    }
}
