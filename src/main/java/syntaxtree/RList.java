package syntaxtree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class RList implements Node {
    private boolean improperList;
    private List<Node> nodes;
    private RList parent;
    private boolean synthetic;  // inserted by the reader from syntactic sugar for quoting

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

    public static final class Builder implements NodeBuilder {
        private final List<NodeBuilder> nodeBuilders = new ArrayList<>();

        private RList.Builder parentListBuilder;
        private int depth;
        private boolean isQuoted;
        private boolean improperList;

        public Builder addNodeBuilder(NodeBuilder nodeBuilder) {
            this.nodeBuilders.add(nodeBuilder);
            return this;
        }

        public RList build() {
            List<Node> nodes = nodeBuilders.stream().map(NodeBuilder::build).toList();
            return new RList(improperList, nodes, false);
        }


        public int getSize() {
            return nodeBuilders.size();
        }
    }
}
