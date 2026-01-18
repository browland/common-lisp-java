package evaluator.special;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import function.Closure;
import syntaxtree.Atom;
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
        List<Atom> bindings = bindingsList.nodes().stream()
                .map(node -> (Atom)node)
                .toList();

        // todo validate bindings - if &rest is present then there should be exactly 1 more binding

        Node body = entireList.get(2);
        BindingEvaluator bindingEvaluator = new BindingEvaluator();
        Closure closure = new Closure(evaluator, bindingEvaluator, environment.capture(), bindings, body);
        return new ClosureValue(closure);
    }
}
