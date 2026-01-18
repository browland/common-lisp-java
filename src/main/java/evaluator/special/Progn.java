package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;

import java.util.List;

public class Progn implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        List<Node> forms = entireList.nodes().subList(1, entireList.nodes().size());
        Value<?> result = Value.nil();
        for(Node node : forms) {
            result = evaluator.evaluate(node, environment);
        }

        return result;
    }
}
