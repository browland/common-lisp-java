package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.RList;
import value.Value;

import java.util.Optional;

public class SpecialFormEvaluator {
    private static final SpecialFormRegistry registry = new SpecialFormRegistry();

    public Optional<Value<?>> evaluate(String operatorName,
                                       RList entireList,
                                       Environment environment,
                                       Evaluator evaluator) {

        SpecialForm specialForm = registry.findByName(operatorName);
        if(specialForm == null) {
            return Optional.empty();
        }

        Value<?> functionValue = specialForm.evaluate(entireList, environment, evaluator);
        return Optional.of(functionValue);
    }
}
