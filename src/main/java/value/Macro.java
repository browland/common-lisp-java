package value;

import syntaxtree.Node;
import syntaxtree.RList;

import java.util.List;

public class Macro {
    // Bindings are Nodes as they can either be plain Atoms, or a list in the case that
    // we're destructuring.
    private final List<Node> bindings;
    private final RList body;

    public Macro(List<Node> bindings,
                 RList body) {
        this.bindings = bindings;
        this.body = body;
    }

    public List<Node> getBindings() {
        return bindings;
    }

    public RList getBody() {
        return body;
    }
}
