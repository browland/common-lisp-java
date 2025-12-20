package function;

import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

// todo inline this - no point it being a function. Then rename DefvarForm to Defvar
public class Defvar implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Map<String, Value<?>> environment) {
        Value<?> nameValue = operands.get(0);
        String name = (String)nameValue.value();
        Value<?> value = operands.get(1);
        environment.put(name, value);

        // returns the name of the variable
        return new Value<>(name, ValueType.SYMBOL);
    }
}
