package syntaxtree;

import java.util.ArrayList;
import java.util.List;

public record RList(int depth,
             String prefix,
             boolean isQuoted,
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

    public RList fromIndex(int index) {
        List<Node> newNodes = nodes.subList(index, nodes.size());
        return new RList(depth, prefix, this.isQuoted, newNodes);
    }

    public static final class Builder implements NodeBuilder {
        private final List<NodeBuilder> nodeBuilders = new ArrayList<>();

        private RList.Builder parentListBuilder;
        private int depth;
        private String prefix;
        private boolean isQuoted;

        Builder parentListBuilder(RList.Builder parentListBuilder) {
            this.parentListBuilder = parentListBuilder;
            return this;
        }

        Builder depth(int depth) {
            this.depth = depth;
            return this;
        }

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder quoted(boolean isQuoted) {
            this.isQuoted = isQuoted;
            return this;
        }

        public boolean isQuoted() {
            return isQuoted;
        }

        public void addNodeBuilder(NodeBuilder nodeBuilder) {
            this.nodeBuilders.add(nodeBuilder);
        }

        public RList build() {
            List<Node> nodes = nodeBuilders.stream().map(NodeBuilder::build).toList();
            return new RList(depth, prefix, isQuoted, nodes);
        }

        public Builder getParentListBuilder() {
            return parentListBuilder;
        }

        public int getDepth() {
            return depth;
        }
    }
}
