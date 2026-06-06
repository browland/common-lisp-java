package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.CharInputStreamValue;
import value.StringValue;
import value.Value;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.List;

public class Open implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // todo simple for now; no on failure parameters
        if(operands.getFirst() instanceof StringValue pathValue) {
            String pathString = pathValue.getValue();
            Path path = Path.of(pathString);

            // default character stream
            try {
                FileInputStream fis = new FileInputStream(path.toFile());
                InputStreamReader inputStreamReader = new InputStreamReader(fis);
                return new CharInputStreamValue(inputStreamReader);
            } catch (FileNotFoundException e) {
                throw new EvaluationException(String.format("Could not open file %s due to %s", path, e));
            }

        }
        return Value.nil();
    }
}
