package evaluator;

import evaluator.env.Environment;
import reader.NewListBuilder;
import syntaxtree.Node;
import value.Value;

import java.util.List;

public class Interpreter {
    private final Evaluator evaluator;
    private final NewListBuilder newListBuilder;
    private final Environment environment;

    public Interpreter() {
        this(new Environment());
    }

    public Interpreter(Environment environment) {
        newListBuilder = new NewListBuilder();

        this.evaluator =  new Evaluator();
        this.environment = environment;
    }

    public Value<?> interpret(String program) {
        List<Node> nodes = newListBuilder.build(program);
        Value<?> result = null;
        for(Node node : nodes) {
            result = evaluator.evaluate(node, environment);
        }

        return result;
    }
}
