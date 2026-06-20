package value;

import function.Function;

public final class FunctionValue extends Value<Function> {
    public FunctionValue(Function value) {
        super(value, ValueType.OPERATOR);
    }

}
