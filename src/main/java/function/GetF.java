package function;

import value.ConsCell;
import value.LispList;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

public class GetF implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Map<String, Value<?>> environment) {
        Value<?> listOperand = operands.get(0);
        Value<?> symbol = operands.get(1);

        if (listOperand.type() != ValueType.LIST) {
            throw new IllegalArgumentException("first operand to getf must be a list");
        }

        LispList lispList = (LispList) listOperand.value();
        ConsCell consCell = lispList.getHeadConsCell();

        Value<?> cdr;
        Value<?> car = consCell.car();
        cdr = consCell.cdr();
        do {
            ConsCell nextConsCellValue = (ConsCell) cdr.value();
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
