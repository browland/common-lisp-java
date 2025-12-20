package evaluator.special;

import evaluator.Evaluator;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;

import java.util.Map;

public class If implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Map<String, Value<?>> environment,
                             Evaluator evaluator) {
        // evaluate test condition.  If nil then we return the evaluation of the second part,
        // otherwise we return the evaluation of the first part.
        Node testNode = entireList.nodes().get(1);

        Value<?> testResult = evaluator.evaluate(testNode, environment);
        if(testResult.equals(Value.nil())) {
            return evaluator.evaluate(entireList.nodes().get(3), environment);
        }
        else {
            return evaluator.evaluate(entireList.nodes().get(2), environment);
        }
    }
}
