package function;

import evaluator.env.Environment;
import value.LispList;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

public class Car implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // single operand - if it's not a LispList then error
        Value<?> operand = operands.get(0);
        if(operand.type() != ValueType.LIST) {
            throw new IllegalArgumentException("Can only invoke car on a list");
        }

        LispList list = (LispList)operand.value();
        return list.getHeadConsCell().car();
    }
}
