package evaluator.special;

import evaluator.Evaluator;
import syntaxtree.RList;
import value.Value;

import java.util.Map;

public interface SpecialForm {
    Value<?> evaluate(RList entireList, Map<String, Value<?>> environment,
                             Evaluator evaluator);
}
