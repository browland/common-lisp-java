package value;

import function.Function;

public class FunctionValue extends Value<Function> {
    public FunctionValue(Function value) {
        super(value, ValueType.OPERATOR);
    }

}
