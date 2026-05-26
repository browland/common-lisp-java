package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class Evenp implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operand = operands.getFirst();
        if(operand instanceof IntegerValue integerValue) {
            int val = integerValue.getValue();
            return val % 2 == 0 ? Value.t() : Value.nil();
        }
        else {
            throw new EvaluationException("even: expects an integer operand");
        }
    }
}
