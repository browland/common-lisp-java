package value;

import syntaxtree.Atom;
import syntaxtree.RList;

import java.util.List;
import java.util.Map;

public class Macro {
    private final Map<String,Value<?>> capturedEnvironment;
    private final List<Atom> bindings;
    private final RList body;

    public Macro(Map<String,Value<?>> capturedEnvironment,
                 List<Atom> bindings,
                 RList body) {
        this.capturedEnvironment = capturedEnvironment;
        this.bindings = bindings;
        this.body = body;
    }

    public List<Atom> getBindings() {
        return bindings;
    }

    public RList getBody() {
        return body;
    }

    public Map<String, Value<?>> getCapturedEnvironment() {
        return capturedEnvironment;
    }
}
