package value;

import function.Closure;

public class ClosureValue extends Value<Closure> {
    public ClosureValue(Closure value) {
        super(value, ValueType.OPERATOR);
    }

    public String toString() {
        return "Closure " + this.hashCode();
    }
}
