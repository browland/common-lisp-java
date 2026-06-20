package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Add implements Function {

    @Override
    public Value<Integer> apply(List<Value<?>> operands, Environment environment) {
        int result = 0;

        for(Value<?> operand : operands) {
            IntegerValue intValue = operand.expectInt("+");
            int opInt = intValue.getValue();
            result += opInt;
        }

        return new IntegerValue(result);
    }
}
