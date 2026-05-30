package value;

import java.util.List;

public class ValuesValue extends Value<List<Value<?>>> {
    public ValuesValue(List<Value<?>> value) {
        super(value, ValueType.VALUES);
    }

    public String toString() {
        return String.join("\n", value.stream().map(Object::toString).toList());
    }
}
