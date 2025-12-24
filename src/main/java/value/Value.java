package value;

import java.util.Objects;

// just holds an evaluation result, but can be of a variety of types, e.g. string literal, quoted list, function, etc.
public class Value<T> {
    protected T value;
    private ValueType type;

    public Value(T value, ValueType type) {
        this.value = value;
        this.type = type;
    }

    public static Value<?> nil() {
        return new Value<>("nil", ValueType.BUILTIN_CONSTANT);
    }

    public static Value<?> t() {
        return new Value<>("T", ValueType.BUILTIN_CONSTANT);
    }

    public static Value<String> of(String value) {
        return new StringValue(value);
    }

    public static <T> Value<T> of(T value, ValueType valueType) {
        return new Value<>(value, valueType);
    }

    public T getValue() {
        return value;
    }

    public ValueType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value<?> value1 = (Value<?>) o;
        return Objects.equals(value, value1.value) && type == value1.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }
}
