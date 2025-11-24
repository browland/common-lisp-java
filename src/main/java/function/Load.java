package function;

import value.Value;
import repl.BatchEvaluator;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Load implements Function {
    private static final String DEFAULT_LOAD_PATH = "/Users/ben/git/lisp/lisp-sources/";

    @Override
    public Value<?> apply(List<Value<?>> operands, Map<String, Value<?>> environment) {
        Value<String> filenameValue = (Value<String>)operands.get(0);
        String filename = filenameValue.value();
        Path absolutePath = Path.of(DEFAULT_LOAD_PATH, filename);

        try {
            FileInputStream fis = new FileInputStream(absolutePath.toFile());
            while (true) {
                int readByte = fis.read();
                if(readByte == -1) {
                    break;
                }

                char c = (char)readByte;
                BatchEvaluator.INSTANCE.consume(c);
            }
            return Value.t();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
