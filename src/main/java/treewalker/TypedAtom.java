package treewalker;

import syntaxtree.Atom;

public sealed class TypedAtom<T> implements ProcessedNode permits StringAtom, IntAtom, FloatAtom, CharAtom, SymbolAtom  {
    private Atom atom;
    protected T value;

    public TypedAtom(Atom atom, T value) {
        this.atom = atom;
        this.value = value;
    }

    T getValue() {
        return value;
    }

    static TypedAtom<?> fromAtom(Atom atom) {
        return TypeCoercer.coerceType(atom);
    }

}
