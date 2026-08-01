package compiler.treewalker;

import syntaxtree.Atom;
import syntaxtree.Node;

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

    public static SymbolAtom toSymbolAtom(Node node) {
        if (node instanceof Atom atom) {
            TypedAtom<?> typedAtom = fromAtom(atom);
            if (typedAtom instanceof SymbolAtom symbolAtom) {
                return symbolAtom;
            }
            else {
                throw new IllegalArgumentException("Expected SymbolAtom but was " + typedAtom);
            }
        }
        else {
            throw new IllegalArgumentException("Expected Atom but was " + node);
        }
    }

    // TODO needed?
    public static IntAtom toIntAtom(Node node) {
        if (node instanceof Atom atom) {
            TypedAtom<?> typedAtom = fromAtom(atom);
            if (typedAtom instanceof IntAtom intAtom) {
                return intAtom;
            }
            else {
                throw new IllegalArgumentException("Expected IntAtom but was " + typedAtom);
            }
        }
        else {
            throw new IllegalArgumentException("Expected Atom but was " + node);
        }
    }

}
