package evaluator.special;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Namespace;
import evaluator.env.Symbols;
import function.Closure;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.ClosureValue;
import value.Symbol;
import value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FLet implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        environment.enterScope();

        addBindingsIntoScope(entireList, environment, evaluator);

        List<Node> nodes = entireList.nodes();
        Value<?> bodyEvaluation = Value.nil();
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
            if(bindingNode instanceof RList binding) {
                Atom name = (Atom)binding.get(0);
                Symbol nameSymbol = Symbols.internSymbol(name.value());

                ClosureValue closureValue = createClosure(binding, environment, evaluator);
                evaluatedBindings.put(nameSymbol, closureValue);
            }
            else {
                throw new IllegalArgumentException("binding in flet not a list");
            }
        }


        for(Symbol symbol : evaluatedBindings.keySet()) {
            environment.setInScope(symbol, evaluatedBindings.get(symbol), Namespace.FUNCTION);
        }

    }

    private ClosureValue createClosure(RList binding,
                                       Environment environment,
                                       Evaluator evaluator) {
        RList bindingsList = (RList)binding.get(1);
        List<Atom> bindings = bindingsList.nodes().stream()
                .map(node -> (Atom)node)
                .toList();

        // todo validate bindings - if &rest is present then there should be exactly 1 more binding

        Node body = binding.get(2);

        BindingEvaluator bindingEvaluator = new BindingEvaluator();
        Closure closure = new Closure(evaluator, bindingEvaluator, environment.capture(), bindings, body);

        return new ClosureValue(closure);
    }
}
