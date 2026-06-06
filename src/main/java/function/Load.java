package function;

import evaluator.Evaluator;
import evaluator.env.Environment;
import reader.NewListBuilder;
import syntaxtree.Node;
import value.Value;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Load implements Function {
    private static final String DEFAULT_LOAD_PATH = "/Users/ben/git/lisp/lisp-sources/";

    private final NewListBuilder newListBuilder = new NewListBuilder();
    private final Evaluator evaluator = new Evaluator();

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<String> filenameValue = (Value<String>)operands.getFirst();
        String filename = filenameValue.getValue();
        Path absolutePath = Path.of(DEFAULT_LOAD_PATH, filename);

        try {
            List<String> lines = Files.readAllLines(absolutePath);
            for(String line : lines) {
                List<Node> nodes = newListBuilder.build(line);
                if(nodes == null) {
                    continue;
                }
                for(Node node : nodes) {
                    evaluator.evaluate(node, environment);
                }
            }
            return Value.t();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
