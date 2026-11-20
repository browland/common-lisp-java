package value;

import exception.EvaluationException;

public final class IntegerValue extends Value<Integer> {

    public IntegerValue(Integer value) {
        super(value, ValueType.INTEGER_LITERAL);
    }

    public static IntegerValue expectIntegerValue(Value<?> value, String operatorName) {
        if (value instanceof IntegerValue) {
            return (IntegerValue) value;
        }
        else {
            throw new EvaluationException(operatorName + ": value not a integer: " + value);
        }
    }

    public String toString() {
        return Integer.toString(value);
    }
}
