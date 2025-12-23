package function;

import evaluator.env.Environment;
import value.ConsCell;
import value.Value;
import value.ValueType;

import java.util.ArrayList;
import java.util.List;

public class ListFunction implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        List<Value<?>> copyOfArgs = new ArrayList<>(operands);
        ConsCell consCell = ConsCell.fromJavaList(copyOfArgs);

        return new Value<>(consCell, ValueType.CONS_CELL);
    }
}
