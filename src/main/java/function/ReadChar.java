package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.CharInputStreamValue;
import value.CharValue;
import value.Value;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReadChar implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.getFirst() instanceof CharInputStreamValue charInputStreamValue) {
            InputStreamReader isr = charInputStreamValue.getValue();
            try {
                int i = isr.read();
                if(i == -1) {
                    throw new EvaluationException("End of file");
                }
                return new CharValue((char)i);
            } catch (IOException e) {
                throw new EvaluationException("read-char: " + e);
            }
        }
        return Value.nil();
    }
}
