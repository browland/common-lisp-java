package function;

import evaluator.env.Environment;
import value.Value;
import value.ValuesValue;

import java.util.ArrayList;
import java.util.List;

public class Values implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        List<Value<?>> operandsCopy = new ArrayList<Value<?>>(operands);
        return new ValuesValue(operandsCopy);
    }
}
