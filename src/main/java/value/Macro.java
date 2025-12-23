package value;

import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.RList;

import java.util.List;

public class Macro {
    private final Environment capturedEnvironment;
    private final List<Atom> bindings;
    private final RList body;

    public Macro(Environment capturedEnvironment,
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

    public Environment getCapturedEnvironment() {
        return capturedEnvironment;
    }
}
