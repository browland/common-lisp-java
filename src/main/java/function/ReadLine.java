package function;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import value.CharInputStreamValue;
import value.StringValue;
import value.SymbolValue;
import value.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class ReadLine implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> possibleStreamValue = operands.getFirst();
        if (possibleStreamValue instanceof CharInputStreamValue streamValue) {
            InputStreamReader reader = streamValue.getValue();
            BufferedReader br = new BufferedReader(reader);

            try {
                String line = br.readLine();
                return line != null ?
                        new StringValue(line) :
                        new SymbolValue(Symbols.nil());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        else {
            throw new IllegalArgumentException("read-line: expect stream");
        }
    }
}
