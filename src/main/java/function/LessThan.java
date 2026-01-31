package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;
import value.ValueType;

import java.util.List;

public class LessThan implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // two operands should be integers
        if(operands.size() != 2) {
            throw new IllegalArgumentException("< expects only two operands");
        }

        Value<?> op1 = operands.get(0);
        Value<?> op2 = operands.get(1);

        if(op1.getType() != ValueType.INTEGER_LITERAL || op2.getType() != ValueType.INTEGER_LITERAL) {
            throw new IllegalArgumentException("< expects two integer operands");
        }

        IntegerValue intValue1 = (IntegerValue) op1;
        IntegerValue intValue2 = (IntegerValue) op2;

        boolean boolResult = intValue1.getValue() < intValue2.getValue();
        return boolResult ? Value.t() : Value.nil();
    }
}
