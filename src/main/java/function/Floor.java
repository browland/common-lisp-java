package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;
import value.ValuesValue;

import java.util.List;

public class Floor implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        IntegerValue integerValue1 = operands.getFirst().expectInt("floor");
        IntegerValue integerValue2 = operands.get(1).expectInt("floor");

        int intValue1 = integerValue1.getValue();
        int intValue2 = integerValue2.getValue();

        int floor = intValue1 / intValue2;
        int remainder = intValue1 % intValue2;

        return new ValuesValue(List.of(new IntegerValue(floor), new IntegerValue(remainder)));
    }
}

