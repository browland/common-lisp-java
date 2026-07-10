package treewalker;

import syntaxtree.Atom;

public final class StringAtom extends TypedAtom<String> {
    public StringAtom(Atom atom, String value) {
        super(atom, value);
    }
}
