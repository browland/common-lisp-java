package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.IntegerValue;
import value.Value;
import value.ValueType;
import value.ValuesValue;

import java.util.List;

public class Floor implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operand1 = operands.getFirst();
        validateOperand(operand1);
        Value<?> operand2 = operands.get(1);
        validateOperand(operand2);

        int intValue1 = (Integer)operand1.getValue();
        int intValue2 = (Integer)operand2.getValue();

        int floor = intValue1 / intValue2;
        int remainder = intValue1 % intValue2;

        return new ValuesValue(List.of(new IntegerValue(floor), new IntegerValue(remainder)));
    }

    private void validateOperand(Value<?> operand) {
        if(ValueType.INTEGER_LITERAL != operand.getType()) {
            throw new EvaluationException("floor: require integer operands");
        }

        Object value = operand.getValue();
        if(! (value instanceof Integer)) {
            throw new IllegalArgumentException("value not of type Integer");
        }
    }
}

