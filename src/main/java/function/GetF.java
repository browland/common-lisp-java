package function;

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

        if(listOperand.type() != ValueType.LIST) {
            throw new IllegalArgumentException("first operand to getf must be a list");
        }

        LispList lispList = (LispList)listOperand.value();

        Map<?, Value<?>> plist = lispList.getPropertyList();
        if(plist.containsKey(symbol)) {
            return plist.get(symbol);
        }
        else {
            return Value.nil();
        }
    }
}
