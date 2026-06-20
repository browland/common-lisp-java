package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Add implements Function {

    @Override
    public Value<Integer> apply(List<Value<?>> operands, Environment environment) {
        int result = 0;

        for(Value<?> operand : operands) {
            switch(operand) {
                case IntegerValue intValue -> {
                    int opInt = intValue.getValue();
                    result += opInt;
                }
                default -> throw new EvaluationException("add: requires integer operands");
            }
        }

        return new IntegerValue(result);
    }
}
