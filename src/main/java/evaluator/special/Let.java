package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Symbol;
import value.Value;

import java.util.HashMap;
import java.util.Map;

public class Let implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {

        environment.enterScope();

        addBindingsIntoScope(entireList, environment, evaluator);

        RList body = (RList)entireList.get(2);
        Value<?> bodyValue = evaluator.evaluate(body, environment);

        environment.leaveScope();
        return bodyValue;
    }

    /**
     * We only add the bindings to the environment at the end (after evaluating the values of the
     * bindings.  Otherwise, we'd be implementing let* which can have dependencies between the
     * bindings.
     */
    private void addBindingsIntoScope(RList entireList,
                                      Environment environment,
                                      Evaluator evaluator) {
        RList bindings = (RList) entireList.get(1);
        Map<Symbol, Value<?>> evaluatedBindings = new HashMap<>();

        for(Node bindingNode : bindings.nodes()) {
            RList bindingList = (RList)bindingNode;
            Atom name = (Atom)bindingList.get(0);
            Symbol nameSymbol = environment.getSymbols().internSymbol(name.value());
            Node value = bindingList.get(1);
            Value<?> evaluatedValue = evaluator.evaluate(value, environment);
            evaluatedBindings.put(nameSymbol, evaluatedValue);
        }

        for(Symbol symbol : evaluatedBindings.keySet()) {
            environment.setInScope(symbol, evaluatedBindings.get(symbol));
        }
    }
}
