package treewalker;

import syntaxtree.Atom;

public sealed class TypedAtom<T> implements ProcessedNode permits StringAtom, IntAtom, FloatAtom, CharAtom, SymbolAtom  {
    private Atom atom;
    protected T value;

    public TypedAtom(Atom atom, T value) {
        this.atom = atom;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public static TypedAtom<?> fromAtom(Atom atom) {
        return TypeCoercer.coerceType(atom);
    }

}
