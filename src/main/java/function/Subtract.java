package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Subtract implements Function {

    @Override
    public Value<Integer> apply(List<Value<?>> operands, Environment environment) {
        int result = 0;

        IntegerValue intValue1 = operands.getFirst().expectInt("-");
        IntegerValue intValue2 = operands.get(1).expectInt("-");

        return new IntegerValue(intValue1.getValue() - intValue2.getValue());
    }
}
