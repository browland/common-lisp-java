package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.StringValue;
import value.Value;

import java.util.List;

public class StringEqual implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operand1 = operands.getFirst();
        Value<?> operand2 = operands.get(1);

        if(operand1 instanceof StringValue stringValue1
                && operand2 instanceof StringValue stringValue2) {
            if(stringValue1.equals(stringValue2)) {
                return Value.t();
            }
            else {
                return Value.nil();
            }
        }
        else {
            throw new EvaluationException("string= requires two string operands");
        }
    }
}
