package function;

import evaluator.env.Environment;
import value.LispList;
import value.Value;
import value.ValueType;

import java.util.ArrayList;
import java.util.List;

public class ListFunction implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        List<Value<?>> copyOfArgs = new ArrayList<>(operands);
        LispList lispList = new LispList(copyOfArgs);

        return new Value<>(lispList, ValueType.LIST);
    }
}
