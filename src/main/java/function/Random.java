package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Random implements Function {
    private java.util.Random random = new java.util.Random();

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // ints only for now
        int limit = ((IntegerValue)operands.getFirst()).getValue();
        int randValue = random.nextInt(limit);
        return new IntegerValue(randValue);
    }
}
