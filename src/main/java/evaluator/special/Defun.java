package evaluator.special;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import function.Closure;
import syntaxtree.Atom;
import syntaxtree.Node;
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
        List<Node> bindings = bindingsList.nodes();

        // todo validate bindings - if &rest is present then there should be exactly 1 more binding

        Node body = entireList.get(3);

        BindingEvaluator bindingEvaluator = new BindingEvaluator();
        Closure closure = new Closure(evaluator, bindingEvaluator, environment.capture(), bindings, body);
        Symbol symbol = Symbols.internSymbol(name);
        environment.setGlobal(symbol, new ClosureValue(closure));

        return new ClosureValue(closure);
    }
}
