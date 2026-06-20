package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;

import java.util.List;

public class GreaterThan implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.size() != 2) {
            throw new IllegalArgumentException("> expects only two operands");
        }

        IntegerValue integerValue1 = operands.getFirst().expectInt(">");
        IntegerValue integerValue2 = operands.get(1).expectInt(">");

        int intValue1 = integerValue1.getValue();
        int intValue2 = integerValue2.getValue();

        boolean boolResult = intValue1 > intValue2;
        return boolResult ? Value.t() : Value.nil();
    }
}
