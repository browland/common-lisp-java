package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;

public class Cond implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // evaluate test condition.  If nil then we return the evaluation of the second part,
        // otherwise we return the evaluation of the first part.
        boolean atArgs = false;
        for(Node form : entireList.nodes()) {
            if (!atArgs) {
                atArgs = true;
                continue;
            }

            if(form instanceof RList rList) {
                Node condition = rList.nodes().get(0);
                Value<?> condResult = evaluator.evaluate(condition, environment);
                if(condResult.equals(Value.t())) {
                    Node toEval = rList.nodes().get(1);
                    return evaluator.evaluate(toEval, environment);
                }
            }
            else {
                throw new IllegalArgumentException("invalid form supplied to cond " + form);
            }
        }
        return Value.nil();
    }
}
