package evaluator;

import evaluator.env.Environment;
import evaluator.macro.MacroExpander;
import evaluator.special.SpecialForm;
import evaluator.special.SpecialFormEvaluator;
import exception.EvaluationException;
import function.Function;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Macro;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Optional;

public class Evaluator {
    private final SpecialFormEvaluator specialFormEvaluator = new SpecialFormEvaluator();
    private final MacroExpander macroExpander = new MacroExpander();
    private final OperatorLookup operatorLookup = new OperatorLookup();
    private final AtomEvaluator atomEvaluator = new AtomEvaluator();
    private final BindingEvaluator bindingEvaluator = new BindingEvaluator();

    public Value<?> evaluate(Node node, Environment environment) {
        // either an atom or a list
        // if an atom then it could be a symbol (in which case look it up) or otherwise a literal value
        // if a list, then pass it back through evaluate() with the environment
        if(node instanceof Atom atom) {
            return atomEvaluator.atomToValueWithLookup(atom, environment);
        }
        else if (node instanceof RList rlist) {
            try {
                return evaluate(rlist, environment);
            }
            catch(EvaluationException e) {
                System.out.println("while evaluating " + node);
                throw e;
            }
        }
        else {
            throw new UnsupportedOperationException("Unhandled node type");
        }
    }

    public Value<?> evaluate(RList list, Environment environment) {
        if(list.size() == 0) {
            throw new EvaluationException("Trying to evaluate form of length zero");
        }
        Node operatorNode = list.get(0);

        // If the operator is itself a list, then we need to evaluate it to get a closure.
        // We can then apply it to the remaining arguments as a form.
        if (operatorNode instanceof RList) {
            Value<?> evaluatedOperatorValue = evaluate((RList)operatorNode, environment);
            if(evaluatedOperatorValue.getType() != ValueType.OPERATOR) {
                throw new EvaluationException("Illegal function %s".formatted(evaluatedOperatorValue), operatorNode);
            }
            Function operator = (Function)evaluatedOperatorValue.getValue();

            // it's always a regular form when the operator is a list.  Special forms are only legal when the operator
            // is a predefined value.
            return applyForm(operator, list, environment);
        } else {
            // If the operator is a special form we need to evaluate it to get its operator implementation (e.g. a closure for a
            // lambda definition).
            Atom operatorAtom = (Atom) operatorNode;
            OperatorType operatorType = operatorLookup.determineOperatorType(operatorAtom, environment);

            if(operatorType == OperatorType.SPECIAL_FORM) {
                SpecialForm specialForm = operatorLookup.lookupSpecialForm(operatorAtom.value(), environment);
                try {
                    return specialFormEvaluator.evaluate(specialForm, list, environment, this);
                }
                catch(EvaluationException e) {
                    System.out.println("while evaluating " + list);
                    throw e;
                }
            }
            else if(operatorType == OperatorType.MACRO) {
                Macro macro = operatorLookup.lookupMacro(operatorAtom.value(), environment);
                Node expandedMacro = macroExpander.expand(macro, list, this, environment);
                try {
                    return evaluate(expandedMacro, environment);
                }
                catch(EvaluationException e) {
                    System.out.println("while evaluating " + list);
                    throw e;
                }
            }
            else {
                // it's a function - evaluate as normal
                Optional<Function> operator = operatorLookup.lookupFunction(operatorAtom.value(), environment);
                if(operator.isPresent()) {
                    return applyForm(operator.get(), list, environment);
                }
                else {
                    throw new IllegalStateException("expected operator but couldn't find it: " + operator);
                }
            }
        }
    }

    private Value<?> applyForm(Function operator,
                               RList fullList,
                               Environment environment) {
        List<? extends Value<?>> operands = fullList.nodes().subList(1, fullList.size()).stream()
                .map(node -> evaluate(node, environment)).toList();

        // Pass only primary value of any values sets
        List<? extends Value<?>> operandsPrimaryValues = operands.stream()
                .map(atomEvaluator::toPrimaryValue)
                .toList();

        return operator.apply((List<Value<?>>) operandsPrimaryValues, environment);
    }

}
