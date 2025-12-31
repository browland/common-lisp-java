package function;

import evaluator.env.Environment;
import value.ConsCell;
import value.Value;
import value.ValueType;

import java.util.List;

public class Car implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // single operand - if it's not a ConsCell then error
        Value<?> operand = operands.getFirst();
        if(operand.getType() != ValueType.CONS_CELL) {
            throw new IllegalArgumentException("Can only invoke car on a list");
        }

        ConsCell consCell = (ConsCell) operand.getValue();
        return consCell.car();
    }
}
