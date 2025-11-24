package function;

import value.LispList;
import value.Value;
import value.ValueType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ListFunction implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Map<String, Value<?>> environment) {
        List<Value<?>> copyOfArgs = new ArrayList<>(operands);
        LispList lispList = new LispList(copyOfArgs);

        return new Value<>(lispList, ValueType.LIST);
    }
}
