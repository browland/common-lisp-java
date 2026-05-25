package function;

import evaluator.env.Environment;
import value.Value;

import java.util.List;

public class Null implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operand = operands.getFirst();
        if(operand.equals(Value.nil())) {
            return Value.t();
        }
        else {
            return Value.nil();
        }
    }
}
