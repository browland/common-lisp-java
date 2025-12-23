package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.RList;
import value.Value;

public interface SpecialForm {
    Value<?> evaluate(RList entireList,
                      Environment environment,
                      Evaluator evaluator);
}
