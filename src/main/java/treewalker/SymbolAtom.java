package treewalker;

import syntaxtree.Atom;

public final class SymbolAtom extends TypedAtom<String> {
    public SymbolAtom(Atom atom, String value) {
        super(atom, value);
    }
}
