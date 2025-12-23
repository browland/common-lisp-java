package function;

import evaluator.env.Environment;
import value.ConsCell;
import value.Value;
import value.ValueType;

import java.util.List;

public class GetF implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> listOperand = operands.get(0);
        Value<?> symbol = operands.get(1);

        if (listOperand.getType() != ValueType.CONS_CELL) {
            throw new IllegalArgumentException("first operand to getf must be a cons cell");
        }

        ConsCell consCell = (ConsCell)listOperand.getValue();

        Value<?> cdr;
        Value<?> car = consCell.car();
        cdr = consCell.cdr();
        do {
            ConsCell nextConsCellValue = (ConsCell) cdr.getValue();
            if (symbol.equals(car)) {
                return nextConsCellValue.car();
            }

            // todo only single steps for now, even though we know we must skip one each time
            car = nextConsCellValue.car();
            cdr = nextConsCellValue.cdr();
        }
        while (!cdr.equals(Value.nil()));

        // No match found
        return Value.nil();
    }
}
