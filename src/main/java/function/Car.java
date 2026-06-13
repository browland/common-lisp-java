package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.ConsCell;
import value.Value;
import value.ValueType;

import java.util.List;

public class Car implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // single operand - if it's not nil or a ConsCell then error
        Value<?> operand = operands.getFirst();
        if(operand.equals(Value.nil())) {
            return Value.nil();
        }
        else if(operand.getType() != ValueType.CONS_CELL) {
            throw new EvaluationException("car expects argument of type list (received "
                    + operands.getFirst().getClass() + ")");
        }

        ConsCell consCell = (ConsCell) operand.getValue();
        return consCell.car();
    }
}
