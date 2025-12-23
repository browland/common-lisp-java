package value;

public class ListValue extends Value<LispList> {
    public ListValue(LispList value) {
        super(value, ValueType.LIST);
    }
}
