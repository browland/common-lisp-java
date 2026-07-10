package treewalker;

import syntaxtree.Atom;

public final class FloatAtom extends TypedAtom<Double> {
    public FloatAtom(Atom atom, Double value) {
        super(atom, value);
    }
}
