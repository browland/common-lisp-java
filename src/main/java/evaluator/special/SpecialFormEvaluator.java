package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.RList;
import value.Value;

public class SpecialFormEvaluator {
    private static final SpecialFormRegistry registry = new SpecialFormRegistry();

    public Value<?> evaluate(SpecialForm specialForm,
                             RList entireList,
                             Environment environment,
                             Evaluator evaluator) {

        return specialForm.evaluate(entireList, environment, evaluator);
    }
}
