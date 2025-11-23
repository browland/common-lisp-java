package function;

import evaluator.Value;
import evaluator.ValueType;

import java.util.List;
import java.util.Map;

public class Format implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Map<String, Value<?>> environment) {
        // The first operand is the output stream to send the string, which is the second operand.
        String stream = (String)operands.get(0).value();
        String value = (String)operands.get(1).value();

        // t is a built-in symbol for constant logical true.  Correct CL behaviour is to treat t as meaning stdout.
        if("t".equals(stream)) {
            // print value to standard out; return nil
            System.out.println(value);
            return new Value<>(null, ValueType.NIL);
        }
        else if("nil".equals(stream)) {
            return operands.get(1);
        }
        else {
            throw new UnsupportedOperationException("Unsupported stream " + stream);
        }
    }
}
