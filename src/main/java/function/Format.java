package function;

import evaluator.env.Environment;
import value.Value;
import value.ValueType;

import java.util.List;

public class Format implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // The first operand is the output stream to send the string, which is the second operand.
        Value<?> streamValue = operands.get(0);
        String value = (String)operands.get(1).getValue();

        // t is a built-in symbol for constant logical true.  Correct CL behaviour is to treat t as meaning stdout.
        if(ValueType.BUILTIN_CONSTANT == streamValue.getType()) {
            if("t".equals(streamValue.getValue())) {
                // print value to standard out; return nil
                System.out.println(value);
                return Value.nil();
            }
            else if(Value.nil().equals(streamValue)) {
                return operands.get(1);
            }
            else {
                throw new UnsupportedOperationException("Unsupported builtin stream constant after evaluation " + streamValue);
            }
        }
        else {
            throw new UnsupportedOperationException("Unsupported stream after evaluation " + streamValue);
        }
    }
}
