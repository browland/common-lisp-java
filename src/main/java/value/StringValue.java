package value;

import exception.EvaluationException;

public final class StringValue extends Value<String> {

    public StringValue(String value) {
        super(value, ValueType.STRING_LITERAL);
    }

    public static StringValue expectStringValue(Value<?> value, String operatorName) {
        if (value instanceof StringValue) {
            return (StringValue) value;
        }
        else {
            throw new EvaluationException(operatorName + ": value not a string: " + value);
        }
    }

    public String toString() {
        return "\"" + value + "\"";
    }
}
