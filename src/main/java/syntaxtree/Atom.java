package syntaxtree;

public record Atom(String value) implements Node {

    public String toString() {
        return value;
    }

    public static Atom expectAtom(Node node) {
        if (node instanceof Atom atom) {
            return atom;
        }
        throw new RuntimeException("Expected atom but was " + node);
    }
}
