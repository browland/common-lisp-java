package value;

import syntaxtree.Node;
import syntaxtree.RList;

import java.util.List;

public class Macro {
    // Bindings are Nodes as they can either be plain Atoms, or a list in the case that
    // we're destructuring.
    private final List<Node> bindings;
    private final List<Node> bodyNodes;
    private final String name;

    public Macro(List<Node> bindings,
                 List<Node> bodyNodes,
                 String name) {
        this.bindings = bindings;
        this.bodyNodes = bodyNodes;
        this.name = name;
    }

    public List<Node> getBindings() {
        return bindings;
    }

    public List<Node> getBodyNodes() {
        return bodyNodes;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("macro %s", name);
    }
}
