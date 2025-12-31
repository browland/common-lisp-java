package function;

import evaluator.env.Environment;
import repl.IncrementalInterpreter;
import value.Value;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class Load implements Function {
    private static final String DEFAULT_LOAD_PATH = "/Users/ben/git/lisp/lisp-sources/";

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<String> filenameValue = (Value<String>)operands.getFirst();
        String filename = filenameValue.getValue();
        Path absolutePath = Path.of(DEFAULT_LOAD_PATH, filename);

        try {
            try (FileInputStream fis = new FileInputStream(absolutePath.toFile())) {
                while (true) {
                    int readByte = fis.read();
                    if (readByte == -1) {
                        break;
                    }

                    char c = (char) readByte;
                    IncrementalInterpreter.INSTANCE.consume(c);
                }
            }
            return Value.t();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
