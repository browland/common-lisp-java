package function;

import evaluator.Value;
import evaluator.ValueType;

import java.util.List;
import java.util.Map;

public class Format implements Function {

    @Override
    public Value<?> apply(List<String> operands, Map<String, String> environment) {
        // The first operand is the output stream to send the string, which is the second operand.
        String stream = operands.get(0);
        String value = operands.get(1);

        // t is a built-in symbol for constant logical true.  Correct CL behaviour is to treat t as meaning stdout.
        if("t".equals(stream)) {
            // print value to standard out; return nil
            System.out.println(value);
            return new Value<>(null, ValueType.NIL);
        }
        else if("nil".equals(stream)) {
            return new Value<>(value, ValueType.LITERAL);
        }
        else {
            throw new UnsupportedOperationException("Unsupported stream " + stream);
        }
    }
}
