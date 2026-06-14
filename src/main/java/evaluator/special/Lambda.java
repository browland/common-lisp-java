package evaluator.special;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import function.Closure;
import syntaxtree.Node;
import syntaxtree.RList;
import value.ClosureValue;
import value.Value;

import java.util.List;

public class Lambda implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        Node operator = entireList.get(0);
        if(operator instanceof RList) {
            throw new IllegalStateException("should not get here - need some better handling?");
        }

        RList bindingsList = (RList)entireList.get(1);
        List<Node> bindings = bindingsList.nodes();

        // todo validate bindings - if &rest is present then there should be exactly 1 more binding

        // evaluate each form in body; return value of last one
        List<Node> forms = entireList.nodes().subList(2, entireList.nodes().size());
        BindingEvaluator bindingEvaluator = new BindingEvaluator();
        Closure closure = new Closure(evaluator, bindingEvaluator, environment.capture(), bindings, forms);

        return new ClosureValue(closure);
    }
}
