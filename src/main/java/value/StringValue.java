package value;

public final class StringValue extends Value<String> {

    public StringValue(String value) {
        super(value, ValueType.STRING_LITERAL);
    }

    public String toString() {
        return "\"" + value + "\"";
    }
}
