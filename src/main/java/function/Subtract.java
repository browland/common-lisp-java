package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Subtract implements Function {

    @Override
    public Value<Integer> apply(List<Value<?>> operands, Environment environment) {
        // terrible assumption for now that operands are all Atoms and their string values parse as integers ... can overflow ... etc etc.
        Value<?> firstOperand = operands.getFirst();
        int result = toInt(firstOperand);

        for(Value<?> operand : operands.subList(1, operands.size())) {
            int opInt = toInt(operand);
            result -= opInt;
        }

        return new IntegerValue(result);
    }

    public int toInt(Value<?> value) {
        if(value instanceof IntegerValue integerValue) {
            return integerValue.getValue();
        }
        else {
            throw new EvaluationException("subtract: requires integer operands");
        }
    }
}
