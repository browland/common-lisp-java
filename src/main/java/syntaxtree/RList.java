package syntaxtree;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public record RList(int depth,
             String prefix,
             boolean isQuoted,
             List<Node> nodes) implements Node {

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

        public Builder addNodeBuilder(NodeBuilder nodeBuilder) {
            this.nodeBuilders.add(nodeBuilder);
            return this;
        }

        public RList build() {
            List<Node> nodes = nodeBuilders.stream().map(NodeBuilder::build).toList();
            return new RList(depth, prefix, isQuoted, nodes);
        }

        public Node popLastNode() {
            Node lastNodeAdded = nodeBuilders.getFirst().build();
            nodeBuilders.clear();
            return lastNodeAdded;
        }

        public Builder getParentListBuilder() {
            return parentListBuilder;
        }

        public int getDepth() {
            return depth;
        }

        public int getSize() {
            return nodeBuilders.size();
        }
    }
}
