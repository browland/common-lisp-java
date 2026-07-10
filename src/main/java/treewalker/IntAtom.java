package treewalker;

import syntaxtree.Atom;

public final class IntAtom extends TypedAtom<Integer> {
    public IntAtom(Atom atom, Integer value) {
        super(atom, value);
    }
}
