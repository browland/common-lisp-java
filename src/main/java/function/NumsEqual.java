package function;

import evaluator.env.Environment;
import syntaxtree.Atom;
import value.Value;
import value.ValueType;

import java.util.List;

/**
 * For the = operator, only defined for numeric args.
 */
public class NumsEqual implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.size() == 2) {
            Value<?> val1 = operands.get(0);
            Value<?> val2 = operands.get(1);

            if(val1.getType() == ValueType.INTEGER_LITERAL && val2.getType() == ValueType.INTEGER_LITERAL) {
                int int1 = (Integer)val1.getValue();
                int int2 = (Integer)val2.getValue();
                boolean result = int1 == int2;
                return result ? Value.t() : Value.nil();
            }
            else {
                throw new IllegalArgumentException("Expect integer args for the = operator");
            }
        }
        else {
            throw new IllegalArgumentException("Expect 2 args to the = operator");
        }
    }
}
