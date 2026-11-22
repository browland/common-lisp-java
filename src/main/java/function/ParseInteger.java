package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.IntegerValue;
import value.StringValue;
import value.Value;

import java.util.List;

public class ParseInteger implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operand1 = operands.getFirst();
        String string = StringValue.expectStringValue(operand1, "parse-integer").getValue();

        try {
            int val = Integer.parseInt(string);
            return new IntegerValue(val);
        } catch (NumberFormatException e) {
            throw new EvaluationException("parse-integer: cannot parse " + string);
        }
    }
}
