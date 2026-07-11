package treewalker;

import java.util.List;

// we consider each node and dispatch to appropriate handler for that thing
public class NodeHandler implements NodeListener {

    public void handleAtom(TypedAtom<?> typedAtom) {
        switch(typedAtom) {
            case StringAtom stringAtom -> handleStringAtom(stringAtom);
            case IntAtom intAtom -> handleIntAtom(intAtom);
            case FloatAtom floatAtom -> handleFloatAtom(floatAtom);
            case CharAtom charAtom -> handleCharAtom(charAtom);
            case SymbolAtom symbolAtom -> handleSymbolAtom(symbolAtom);
            default -> throw new UnsupportedOperationException("not supported: " + typedAtom);
        }
    }

    @Override
    public void startForm() {
        System.out.println("start form");
    }

    @Override
    public void applyForm(List<TypedAtom<?>> typedAtoms) {
        System.out.println("apply form with " + typedAtoms);
    }

    private static void handleStringAtom(StringAtom stringAtom) {
        System.out.printf("encountered string atom %s with value %s%n", stringAtom, stringAtom.getValue());
    }

    private static void handleIntAtom(IntAtom intAtom) {
        System.out.printf("encountered int atom %s with value %d%n", intAtom, intAtom.getValue());
    }

    private static void handleFloatAtom(FloatAtom floatAtom) {
        System.out.printf("encountered float atom: %s with value %f%n", floatAtom, floatAtom.getValue());
    }

    private static void handleCharAtom(CharAtom charAtom) {
        System.out.printf("encountered char atom: %s with value %s%n", charAtom, charAtom.getValue());
    }

    private static void handleSymbolAtom(SymbolAtom symbolAtom) {
        System.out.printf("encountered symbol atom: %s with value %s%n", symbolAtom, symbolAtom.getValue());
    }
}
