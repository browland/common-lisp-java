package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.CharInputStreamValue;
import value.CharValue;
import value.StringValue;
import value.Value;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

public class Close implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.getFirst() instanceof CharInputStreamValue charInputStreamValue) {
            InputStreamReader isr = charInputStreamValue.getValue();
            try {
                isr.close();
                return Value.t();
            } catch (IOException e) {
                throw new EvaluationException("Could not close file");
            }
        }
        return Value.nil();
    }
}
