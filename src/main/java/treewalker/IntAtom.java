package treewalker;

import syntaxtree.Atom;

public final class IntAtom extends TypedAtom<Integer> {
    public IntAtom(Atom atom, Integer value) {
        super(atom, value);
    }

    long getFixNum() {
        // Shift bits left by 3 places and tag the low 3 bits as 001 to indicate this is a fixnum.
        long fixNum = (long) value << 3;
        return fixNum | 0x1;
    }
}
