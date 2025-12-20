package evaluator.special;

import evaluator.Evaluator;
import syntaxtree.RList;
import value.Value;

import java.util.Map;
import java.util.Optional;

public class SpecialFormEvaluator {
    private static final SpecialFormRegistry registry = new SpecialFormRegistry();

    public Optional<Value<?>> evaluate(String operatorName,
                                       RList entireList,
                                       Map<String, Value<?>> environment,
                                       Evaluator evaluator) {

        SpecialForm specialForm = registry.findByName(operatorName);
        if(specialForm == null) {
            return Optional.empty();
        }

        Value<?> functionValue = specialForm.evaluate(entireList, environment, evaluator);
        return Optional.of(functionValue);
    }
}
