package treewalker;

import syntaxtree.Atom;

import java.util.List;

public interface NodeListener {
    void handleAtom(TypedAtom<?> atom);
    void handleForm(List<TypedAtom<?>> typedAtoms);
}
