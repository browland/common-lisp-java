package function;

import evaluator.env.Environment;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

public class Add implements Function {

    @Override
    public Value<Integer> apply(List<Value<?>> operands, Environment environment) {
        // terrible assumption for now that operands are all Atoms and their string values parse as integers ... can overflow ... etc etc.
        int result = 0;

        for(Value<?> operand : operands) {
            if(ValueType.INTEGER_LITERAL != operand.type()) {
                throw new IllegalArgumentException("Only works with integer operands at the mo");
            }

            Object value = operand.value();
            if(! (value instanceof Integer)) {
                throw new IllegalArgumentException("value not of type Integer");
            }

            int intValue = (Integer)value;
            result += intValue;

        }

        return new Value<>(result, ValueType.INTEGER_LITERAL);
    }
}
