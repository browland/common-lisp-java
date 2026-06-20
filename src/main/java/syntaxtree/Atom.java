package syntaxtree;

public record Atom(String value) implements Node {

    public String toString() {
        return value;
    }
}
