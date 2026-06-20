package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Multiply implements Function {

    @Override
    public Value<Integer> apply(List<Value<?>> operands, Environment environment) {
        int result = 1;

        for(Value<?> operand : operands) {
            IntegerValue integerValue = operand.expectInt("*");
            int intValue = integerValue.getValue();
            result *= intValue;
        }

        return new IntegerValue(result);
    }
}
