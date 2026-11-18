package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class OnePlus implements Function {

    @Override
    public Value<Integer> apply(List<Value<?>> operands, Environment environment) {
        int result = 0;

        IntegerValue operand = operands.getFirst()
                .expectInt("1+");

        int opInt = operand.getValue();
        result = opInt + 1;
        return new IntegerValue(result);
    }
}
