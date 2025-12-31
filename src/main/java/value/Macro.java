package value;

import syntaxtree.Atom;
import syntaxtree.RList;

import java.util.List;

public class Macro {
    private final List<Atom> bindings;
    private final RList body;

    public Macro(List<Atom> bindings,
                 RList body) {
        this.bindings = bindings;
        this.body = body;
    }

    public List<Atom> getBindings() {
        return bindings;
    }

    public RList getBody() {
        return body;
    }
}
