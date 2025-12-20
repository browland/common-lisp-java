package evaluator.special;

import evaluator.Evaluator;
import function.Closure;
import function.Function;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

public class Lambda implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Map<String, Value<?>> environment,
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

        RList body = (RList) entireList.get(2);
        Closure closure = new Closure(evaluator, environment, bindings, body, null);
        return new Value<Function>(closure, ValueType.OPERATOR);
    }
}
