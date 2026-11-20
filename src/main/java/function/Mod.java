package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Mod implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        IntegerValue numberValue = operands.getFirst().expectInt("mod");
        IntegerValue modulusValue = operands.get(1).expectInt("mod");

        int result = numberValue.getValue() % modulusValue.getValue();

        return new IntegerValue(result);
    }
}
