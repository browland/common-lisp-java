package treewalker;

import reader.NodeBuilder;
import syntaxtree.Atom;
import syntaxtree.Node;

import java.util.List;

// Beginnings of compiler and might end up being retro-fitted to the existing interpreter as a general case of tree-
// walking.
public class TreeWalker {

    public static void main(String[] args) {
        NodeBuilder nodeBuilder = new NodeBuilder();

        // String atom
        String program = "\"hello world\"";
        List<Node> nodes = nodeBuilder.build(program);
        walkTree(nodes);

        // int atom
        program = "1";
        nodes = nodeBuilder.build(program);
        walkTree(nodes);

        // float atom
        program = "1.23";
        nodes = nodeBuilder.build(program);
        walkTree(nodes);

        // char atom
        program = "#\\c";
        nodes = nodeBuilder.build(program);
        walkTree(nodes);
    }

    private static void walkTree(List<Node> nodes) {
        for (Node node : nodes) {
            if (node instanceof Atom atom) {
                handleAtom(atom);
            }
        }
    }

    private static void handleAtom(Atom atom) {
        TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
        switch(typedAtom) {
            case StringAtom stringAtom -> handleStringAtom(stringAtom);
            case IntAtom intAtom -> handleIntAtom(intAtom);
            case FloatAtom floatAtom -> handleFloatAtom(floatAtom);
            case CharAtom charAtom -> handleCharAtom(charAtom);
            default -> throw new UnsupportedOperationException("not supported: " + atom);
        }
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
}
