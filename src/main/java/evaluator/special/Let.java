package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Symbol;
import value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Let implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {

        environment.enterScope();

        addBindingsIntoScope(entireList, environment, evaluator);

        List<Node> nodes = entireList.nodes();
        Value<?> bodyEvaluation = null;
        for(Node bodyNode : nodes.subList(2, nodes.size())) {
            bodyEvaluation = evaluator.evaluate(bodyNode, environment);
        }

        environment.leaveScope();
        return bodyEvaluation;
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
            Symbol nameSymbol = Symbols.internSymbol(name.value());
            Node value = bindingList.get(1);
            Value<?> evaluatedValue = evaluator.evaluate(value, environment);
            evaluatedBindings.put(nameSymbol, evaluatedValue);
        }

        for(Symbol symbol : evaluatedBindings.keySet()) {
            environment.setInScope(symbol, evaluatedBindings.get(symbol));
        }
    }
}
