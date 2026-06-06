package value;

import syntaxtree.Node;
import syntaxtree.RList;

import java.util.List;

public class Macro {
    // Bindings are Nodes as they can either be plain Atoms, or a list in the case that
    // we're destructuring.
    private final List<Node> bindings;
    private final RList body;
    private final String name;

    public Macro(List<Node> bindings,
                 RList body,
                 String name) {
        this.bindings = bindings;
        this.body = body;
        this.name = name;
    }

    public List<Node> getBindings() {
        return bindings;
    }

    public RList getBody() {
        return body;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("macro %s", name);
    }
}
