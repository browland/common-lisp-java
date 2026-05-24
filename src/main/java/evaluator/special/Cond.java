package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import exception.EvaluationException;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;

public class Cond implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // Skip the 'cond' atom itself
        boolean atArgs = false;
        for(Node form : entireList.nodes()) {
            if (!atArgs) {
                atArgs = true;
                continue;
            }

            if(form instanceof RList rList) {
                Node condition = rList.nodes().getFirst();
                Value<?> condResult = evaluator.evaluate(condition, environment);
                if(condResult.equals(Value.t())) {
                    Value<?> resultValue = null;
                    for(Node toEval : rList.nodes().subList(1, rList.nodes().size())) {
                        resultValue = evaluator.evaluate(toEval, environment);
                    }
                    return resultValue;
                }
            }
            else {
                throw new EvaluationException("invalid form supplied to cond " + form);
            }
        }
        return Value.nil();
    }
}
