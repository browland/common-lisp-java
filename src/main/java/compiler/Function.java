package compiler;

import java.util.ArrayList;
import java.util.List;

/**
 * A function is made up by an operator followed by the appropriate list of operands which can be either primitives
 * or more Functions (which implies another call is being made).
 * E.g.:
 *  [+, 1, Function]
 *  could relate to the top-level form in:
 *  (+ 1 (+ 1 2))
 *
 */
public class Function {
    private final String name;
    private final List<Object> parts = new ArrayList<>();

    public Function(String name) {
        this.name = name;
    }

    public void pushInt(int i) {
        parts.add(i);
    }

    public void pushOperator(String op) {
        Operator operator = new Operator(op);
        parts.add(operator);
    }

    public void pushFunction(Function fxn) {
        parts.add(fxn);
    }

    public void pushReturnValue() {
        parts.add(new ReturnValue());
    }

    public String getName() {
        return name;
    }

    public List<Object> getParts() {
        return parts;
    }
}
