package evaluator;

import evaluator.env.Environment;
import evaluator.macro.MacroEvaluator;
import evaluator.special.SpecialFormEvaluator;
import function.Function;
import function.FunctionRegistry;
import reader.CharacterReader;
import syntaxtree.*;
import value.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class Evaluator {
    private static final Set<String> BUILTIN_CONSTANTS = Set.of("t", "nil");

    private final FunctionRegistry functionRegistry = new FunctionRegistry();
    private final SpecialFormEvaluator specialFormEvaluator = new SpecialFormEvaluator();
    private final MacroEvaluator macroEvaluator = new MacroEvaluator();

    public Value<?> evaluate(RList list, Environment environment) {
        Function operator;
        Node operatorNode = list.get(0);

        // If the operator is itself a list, then we need to evaluate it to get a closure.
        // We can then apply it to the remaining arguments as a form.
        if (operatorNode instanceof RList) {
            Value<?> evaluatedOperatorValue = evaluate((RList)operatorNode, environment);
            operator = (Function)evaluatedOperatorValue.getValue();

            // it's always a regular form when the operator is a list.  Special forms are only legal when the operator
            // is a predefined value.
            return applyForm(operator, list, environment);
        } else {
            // If the operator is a special form we need to evaluate it to get its operator implementation (e.g. a closure for a
            // lambda definition).
            // todo: maybe some special forms return a straightforward result when eval'd e.g. defvar, so not always a
            //       recursive call and don't need to treat all special forms as functions/operators?
            Atom operatorAtom = (Atom) operatorNode;
            Optional<Value<?>> optionalSpecialFormResult =
                    specialFormEvaluator.evaluate(operatorAtom.value(), list, environment, this);
            if(optionalSpecialFormResult.isPresent()) {
                return optionalSpecialFormResult.get();
            }

            // This might be a macro; try to look it up and evaluate it.
            Optional<Value<?>> optionalMacroResult = macroEvaluator.evaluate(operatorAtom.value(), list, environment, this);
            if(optionalMacroResult.isPresent()) {
                return optionalMacroResult.get();
            }

            // We did not match on any special form or macro, so this must be a regular form.
            // We know enough to handle it here.
            operator = functionRegistry.findByName(operatorAtom.value());
            if (operator == null) {
                String operatorName = operatorAtom.value();
                Symbol operatorSymbol = environment.getSymbols().internSymbol(operatorName);
                Optional<Value<?>> possibleOperator = environment.get(operatorSymbol);
                if(possibleOperator.isEmpty()) {
                    throw new RuntimeException("ERROR: Can't find definition for operator " + operatorAtom.value());
                }

                Value<?> resolvedOperator = possibleOperator.get();

                if (ValueType.OPERATOR == resolvedOperator.getType()) {
                    operator = (Function) resolvedOperator.getValue();
                }
            }
            if (operator == null) {
                throw new IllegalArgumentException("Could not find operator " + operatorAtom);
            }
            return applyForm(operator, list, environment);
        }
    }

    private Value<?> applyForm(Function operator,
                               RList fullList,
                               Environment environment) {
        List<? extends Value<?>> operands = fullList.nodes().subList(1, fullList.size()).stream()
                .map(node -> evaluate(node, environment)).toList();
        return operator.apply((List<Value<?>>) operands, environment);
    }

    public Value<?> evaluate(Node node, Environment environment) {
        // either an atom or a list
        // if an atom then it could be a symbol (in which case look it up) or otherwise a literal value
        // if a list, then pass it back through evaluate() with the environment
        if(node instanceof Atom atom) {
            return atomToValue(atom, environment);
        }
        else if (node instanceof RList rlist) {
            return evaluate(rlist, environment);
        }
        else {
            throw new UnsupportedOperationException("Unhandled node type");
        }
    }

    private Value<?> atomToValue(Atom atom, Environment environment) {
        String atomStringValue = atom.value();
        if(BUILTIN_CONSTANTS.contains(atomStringValue)) {
            return new Value<>(atomStringValue, ValueType.BUILTIN_CONSTANT);
        }
        else if(atomStringValue.startsWith(":")) {
            // keyword symbol - a literal symbol which evaluates to itself
            Symbol symbol = environment.getSymbols().internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
        else if(atomStringValue.startsWith("\"") && atomStringValue.endsWith("\"")) {
            String stringWithoutQuotes = atomStringValue.substring(1, atomStringValue.length()-1);
            return new StringValue(stringWithoutQuotes);
        }
        else {
            // could be in the environment; otherwise fall back to int
            Symbol symbol = environment.getSymbols().internSymbol(atomStringValue);
            Optional<Value<?>> possibleValue = environment.get(symbol);
            if(possibleValue.isPresent()) {
                return possibleValue.get();
            }

            int intValue = Integer.parseInt(atomStringValue);
            return new IntegerValue(intValue);
        }
    }
}
