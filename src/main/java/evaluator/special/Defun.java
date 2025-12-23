package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import function.Closure;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.List;

public class Defun implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        String name = ((Atom)entireList.get(1)).value();

        RList bindingsList = (RList)entireList.get(2);
        List<Atom> bindings = bindingsList.nodes().stream()
                .map(node -> (Atom)node)
                .toList();

        // todo validate bindings - if &rest is present then there should be exactly 1 more binding

        RList body = (RList) entireList.get(3);

        Closure closure = new Closure(evaluator, environment.capture(), bindings, body);
        // todo assuming global
        environment.setGlobal(name, new Value<>(closure, ValueType.OPERATOR));

        return new Value<>(closure, ValueType.OPERATOR);
    }
}
