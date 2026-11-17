package evaluator;

import evaluator.env.Environment;
import function.FunctionDefinitions;
import reader.NodeBuilder;
import syntaxtree.Node;
import value.Value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Responsible for taking (String) lines from a program and returning a Value.
 */
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

        FunctionDefinitions.addFunctionDefinitions(environment);
    }

    /**
     * All forms in the the provided program must be completed; e.g. this will probably fail if you pass individual lines
     * from a program.  In that case, use interpretPartial().
     */
    public Value<?> interpret(String program) {
        List<Node> nodes = nodeBuilder.build(program);
        Value<?> result = null;
        for(Node node : nodes) {
            result = evaluator.evaluate(node, environment);
        }

        return result;
    }

    /**
     * It's possible to pass a partial program, for example while loading lines from a script or the repl.
     * If partialProgram doesn't represent a completed form then we return empty, otherwise we return the value.
     */
    public Optional<List<Value<?>>> interpretPartial(String partialProgram) {
        List<Value<?>> results = new ArrayList<>();
        List<Node> nodes = nodeBuilder.build(partialProgram);
        if (nodes == null || nodes.isEmpty()) {
            return Optional.empty();
        }

        Value<?> result = null;
        for(Node node : nodes) {
            result = evaluator.evaluate(node, environment);
            if (result == null) {
                throw new IllegalStateException("Got null from evaluate()!");
            }
            results.add(result);
        }

        return Optional.of(results);
    }
}
