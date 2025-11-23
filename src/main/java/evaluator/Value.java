package evaluator;

// just holds an evaluation result
public record Value<T>(T value,
                       ValueType type) {
}
