package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Namespace;
import evaluator.env.Symbols;
import exception.EvaluationException;
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
            if(bindingNode instanceof RList bindingList) {
                if (bindingList.get(0) instanceof Atom(String bindingName)) {
                    Symbol nameSymbol = Symbols.internSymbol(bindingName);
                    Node value = bindingList.get(1);
                    Value<?> evaluatedValue = evaluator.evaluate(value, environment);
                    evaluatedBindings.put(nameSymbol, evaluatedValue);
                }
                else {
                    throw new EvaluationException("unexpected binding name - was a list and not an atom");
                }
            }
            else {
                throw new EvaluationException("let binding name must be an atom but was %s".formatted(bindingNode));
            }
        }

        for(Symbol symbol : evaluatedBindings.keySet()) {
            environment.declareLexical(symbol, evaluatedBindings.get(symbol), Namespace.VARIABLE);
        }
    }
}
