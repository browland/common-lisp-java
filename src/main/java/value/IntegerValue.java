package value;

public final class IntegerValue extends Value<Integer> {

    public IntegerValue(Integer value) {
        super(value, ValueType.INTEGER_LITERAL);
    }

    public String toString() {
        return Integer.toString(value);
    }
}
