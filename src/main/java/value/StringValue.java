package value;

public class StringValue extends Value<String> {

    public StringValue(String value) {
        super(value, ValueType.STRING_LITERAL);
    }
}
