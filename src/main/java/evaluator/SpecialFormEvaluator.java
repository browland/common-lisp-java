package evaluator;

import function.Closure;
import function.Defvar;
import function.Function;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class SpecialFormEvaluator {
    private static final Set<String> SPECIAL_FORM_OPERATORS = Set.of("lambda", "defun", "defvar", "setf");

    public Optional<Value<?>> evaluate(String operatorName,
                                       RList entireList,
                                       Map<String, Value<?>> environment,
                                       Evaluator evaluator) {

        if(!SPECIAL_FORM_OPERATORS.contains(operatorName)) {
            return Optional.empty();
        }

        if ("lambda".equals(operatorName)) {
            Function operator = evaluateLambda(entireList, environment, evaluator);
            Value<Function> functionValue = new Value<>(operator, ValueType.OPERATOR);
            return Optional.of(functionValue);
        } else if ("defun".equals(operatorName)) {
            Closure closure = evaluateDefun(entireList, environment, evaluator);
            environment.put(closure.optionalName(), new Value<>(closure, ValueType.OPERATOR));
            Value<Closure> closureValue = new Value<>(closure, ValueType.OPERATOR);
            return Optional.of(closureValue);
        } else if ("defvar".equals(operatorName)) {
            Value<?> evaluationResult = evaluateDefvar(entireList, environment, evaluator);
            return Optional.of(evaluationResult);
        } else if ("setf".equals(operatorName)) {
            Value<?> evaluationResult = evaluateSetf(entireList, environment, evaluator);
            return Optional.of(evaluationResult);
        } else {
            throw new UnsupportedOperationException("unsupported special form " + operatorName);
        }
    }

    private Closure evaluateLambda(RList list,
                                   Map<String,Value<?>> capturedEnvironment,
                                   Evaluator evaluator) {
        Node operator = list.get(0);
        if(operator instanceof RList) {
            throw new IllegalStateException("should not get here - need some better handling?");
        }

        RList bindingsList = (RList)list.get(1);
        List<String> bindings = bindingsList.nodes().stream().map(node -> {
            Atom atom = (Atom)node;
            return atom.value();
        }).toList();

        RList body = (RList) list.get(2);
        return new Closure(evaluator, capturedEnvironment, bindings, body, null);
    }

    private Closure evaluateDefun(RList list,
                                  Map<String,Value<?>> capturedEnvironment,
                                  Evaluator evaluator) {
        String name = ((Atom)list.get(1)).value();

        RList bindingsList = (RList)list.get(2);
        List<String> bindings = bindingsList.nodes().stream().map(node -> {
            Atom atom = (Atom)node;
            return atom.value();
        }).toList();

        RList body = (RList) list.get(3);
        return new Closure(evaluator, capturedEnvironment, bindings, body, name);
    }

    private Value<?> evaluateDefvar(RList list,
                                    Map<String, Value<?>> environment,
                                    Evaluator evaluator) {
        Defvar defvar = new Defvar();

        Atom nameAtom = (Atom) list.get(1);
        if(nameAtom.prefix() != null || nameAtom.suffix() != null) {
            throw new IllegalArgumentException("name for defvar must be a symbol: [" + nameAtom + "]");
        }

        String name = nameAtom.value();
        Value<?> nameValue = new Value<>(name, ValueType.SYMBOL);
        Node valueNode = list.get(2);
        Value<?> valueValue = evaluator.evaluateOperand(valueNode, environment);

        return defvar.apply(List.of(nameValue, valueValue), environment);
    }

    private Value<?> evaluateSetf(RList list,
                                  Map<String, Value<?>> environment,
                                  Evaluator evaluator) {
        Atom symbolAtom = (Atom) list.get(1);
        if(symbolAtom.prefix() != null || symbolAtom.suffix() != null) {
            throw new IllegalArgumentException("name for defvar must be a symbol: [" + symbolAtom + "]");
        }

        String name = symbolAtom.value();

        // for now we only implement setf for lists.  The list must already exist at the given symbol.
        if(!environment.containsKey(name)) {
            throw new UnsupportedOperationException("Cannot setf a list which isn't bound: " + name);
        }

        Value<?> boundConsOrNil = environment.get(name);
        if(!(boundConsOrNil.type() == ValueType.CONS_CELL || boundConsOrNil.equals(Value.nil()))) {
            throw new IllegalArgumentException("can only setf into a cons cell for now: " + name);
        }

        // evaluate the value being set to the symbol
        Value<?> value = evaluator.evaluateOperand(list.nodes().get(2), environment);

        // ensure it's a list for now
        if(value.type() != ValueType.CONS_CELL) {
            throw new IllegalArgumentException("can only setf a cons cell for now: " + value);
        }

        environment.put(name, value);
        return value;
    }
}
