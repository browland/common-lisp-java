package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

/**
 * For the = operator, only defined for numeric args.
 */
public class NumsEqual implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.size() == 2) {
            IntegerValue integerValue1 = operands.getFirst().expectInt("=");
            IntegerValue integerValue2 = operands.get(1).expectInt("=");

            return integerValue1.getValue().equals(integerValue2.getValue()) ? Value.t() : Value.nil();
        }
        else {
            throw new IllegalArgumentException("Expect 2 args to the = operator");
        }
    }
}
