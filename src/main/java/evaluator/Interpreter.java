package evaluator;

import evaluator.env.Environment;
import reader.NodeBuilder;
import syntaxtree.Node;
import value.Value;

import java.util.List;

public class Interpreter {
    private final Evaluator evaluator;
    private final NodeBuilder nodeBuilder;
    private final Environment environment;

    public Interpreter() {
        this(new Environment());
    }

    public Interpreter(Environment environment) {
        nodeBuilder = new NodeBuilder();

        this.evaluator =  new Evaluator();
        this.environment = environment;
    }

    public Value<?> interpret(String program) {
        List<Node> nodes = nodeBuilder.build(program);
        Value<?> result = null;
        for(Node node : nodes) {
            result = evaluator.evaluate(node, environment);
        }

        return result;
    }
}
