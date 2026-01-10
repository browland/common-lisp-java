package evaluator.special;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import function.Closure;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.ClosureValue;
import value.Symbol;
import value.Value;

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

        BindingEvaluator bindingEvaluator = new BindingEvaluator();
        Closure closure = new Closure(evaluator, bindingEvaluator, environment.capture(), bindings, body);
        Symbol symbol = Symbols.internSymbol(name);
        // todo assuming global
        environment.setGlobal(symbol, new ClosureValue(closure));

        return new ClosureValue(closure);
    }
}
