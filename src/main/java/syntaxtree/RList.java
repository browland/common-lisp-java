package syntaxtree;

import java.util.ArrayList;
import java.util.List;

public record RList(int depth,
             String prefix,
             List<Node> nodes) implements Node {

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nodes.size(); i++) {
            Object node = nodes.get(i);
            for (int j = 0; j < depth; j++) {
                sb.append(" ");
            }
            sb.append(node);
            // Don't add a newline if we just wrote an RList as it has its own terminating newline, or if this is the
            // last node in the top-level list.
            if (!(node instanceof RList) && (!(depth == 0 && i == nodes.size() - 1))) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    public Node get(int i) {
        return nodes.get(i);
    }

    public int size() {
        return nodes.size();
    }

    static final class Builder implements NodeBuilder {
        private final List<NodeBuilder> nodeBuilders = new ArrayList<>();

        private RList.Builder parentListBuilder;
        private int depth;
        private String prefix;

        Builder parentListBuilder(RList.Builder parentListBuilder) {
            this.parentListBuilder = parentListBuilder;
            return this;
        }

        Builder depth(int depth) {
            this.depth = depth;
            return this;
        }

        Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        void addNodeBuilder(NodeBuilder nodeBuilder) {
            this.nodeBuilders.add(nodeBuilder);
        }

        public RList build() {
            List<Node> nodes = nodeBuilders.stream().map(NodeBuilder::build).toList();
            return new RList(depth, prefix, nodes);
        }

        public Builder getParentListBuilder() {
            return parentListBuilder;
        }
    }
}
