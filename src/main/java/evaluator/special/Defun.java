package evaluator.special;

import evaluator.Evaluator;
import function.Closure;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

public class Defun implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Map<String, Value<?>> environment,
                             Evaluator evaluator) {
        String name = ((Atom)entireList.get(1)).value();

        RList bindingsList = (RList)entireList.get(2);
        List<Atom> bindings = bindingsList.nodes().stream()
                .map(node -> (Atom)node)
                .toList();

        // todo validate bindings - if &rest is present then there should be exactly 1 more binding

        RList body = (RList) entireList.get(3);

        Closure closure = new Closure(evaluator, environment, bindings, body, name);
        environment.put(closure.optionalName(), new Value<>(closure, ValueType.OPERATOR));

        return new Value<>(closure, ValueType.OPERATOR);
    }
}
