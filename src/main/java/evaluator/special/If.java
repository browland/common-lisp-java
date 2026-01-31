package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;

public class If implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // evaluate test condition.  If nil then we return the evaluation of the second part,
        // otherwise we return the evaluation of the first part.
        Node testNode = entireList.nodes().get(1);

        Value<?> testResult = evaluator.evaluate(testNode, environment);
        if(testResult.equals(Value.nil())) {
            if(entireList.nodes().size() > 3) {
                return evaluator.evaluate(entireList.nodes().get(3), environment);
            }
            else {
                return Value.nil();
            }
        }
        else {
            return evaluator.evaluate(entireList.nodes().get(2), environment);
        }
    }
}
