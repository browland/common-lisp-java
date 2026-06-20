package syntaxtree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RList implements Node {
    private final boolean improperList;
    private final List<Node> nodes;
    private final boolean synthetic;  // inserted by the reader from syntactic sugar for quoting
    private RList parent;

    // Used for things like macros where we're generating a list dynamically rather than via the reader
    public RList() {
        this(false, new ArrayList<>(), false);
    }

    public RList(boolean improperList, List<Node> nodes, boolean synthetic) {
        this.improperList = improperList;
        this.nodes = nodes;
        this.synthetic = synthetic;
    }

    public String toString() {
        String nodeStrings = nodes.stream()
                .map(Object::toString)
                .collect(Collectors.joining(" ", "(", ")"));
        return nodeStrings;
    }

    public Node get(int i) {
        return nodes.get(i);
    }

    public int size() {
        return nodes.size();
    }

    // adding methods to stop breakage after migrating from record
    public List<Node> nodes() {
        return nodes;
    }

    public boolean improperList() {
        return improperList;
    }

    public boolean isSynthetic() {
        return synthetic;
    }

    public void add(Node node) {
        nodes.add(node);
    }

    public RList getParent() {
        return parent;
    }

    public void setParent(RList parent) {
        this.parent = parent;
    }
}
