package value;

// just holds an evaluation result, but can be of a variety of types, e.g. string literal, quoted list, function, etc.
public record Value<T>(T value,
                       ValueType type) {

    public static Value<?> nil() {
        return new Value<>("nil", ValueType.BUILTIN_CONSTANT);
    }

    public static Value<?> t() {
        return new Value<>("T", ValueType.BUILTIN_CONSTANT);
    }

    public static Value<String> of(String value) {
        return new Value<String>(value, ValueType.STRING_LITERAL);
    }

    public static <T> Value<T> of(T value, ValueType valueType) {
        return new Value<T>(value, valueType);
    }
}
