package evaluator;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import evaluator.macro.MacroExpander;
import evaluator.special.SpecialForm;
import evaluator.special.SpecialFormEvaluator;
import function.Function;
import syntaxtree.*;
import value.*;

import java.util.List;
import java.util.Optional;

public class Evaluator {
    private final SpecialFormEvaluator specialFormEvaluator = new SpecialFormEvaluator();
    private final MacroExpander macroExpander = new MacroExpander();
    private final OperatorLookup operatorLookup = new OperatorLookup();

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

    public Value<?> evaluate(RList list, Environment environment) {
        Node operatorNode = list.get(0);

        // If the operator is itself a list, then we need to evaluate it to get a closure.
        // We can then apply it to the remaining arguments as a form.
        if (operatorNode instanceof RList) {
            Value<?> evaluatedOperatorValue = evaluate((RList)operatorNode, environment);
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
                return specialFormEvaluator.evaluate(specialForm, list, environment, this);
            }
            else if(operatorType == OperatorType.MACRO) {
                Macro macro = operatorLookup.lookupMacro(operatorAtom.value(), environment);
                RList expandedMacro = macroExpander.expand(macro, list);
                return evaluate(expandedMacro, environment);
            }
            else {
                // it's a function - evaluate as normal
                Function operator = operatorLookup.lookupFunction(operatorAtom.value(), environment);
                return applyForm(operator, list, environment);
            }
        }
    }

    private Value<?> applyForm(Function operator,
                               RList fullList,
                               Environment environment) {
        List<? extends Value<?>> operands = fullList.nodes().subList(1, fullList.size()).stream()
                .map(node -> evaluate(node, environment)).toList();
        return operator.apply((List<Value<?>>) operands, environment);
    }

    private Value<?> atomToValue(Atom atom, Environment environment) {
        String atomStringValue = atom.value();
        if(atomStringValue.startsWith(":")) {
            // keyword symbol - a literal symbol which evaluates to itself
            Symbol symbol = environment.internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
        else if(atomStringValue.startsWith("\"") && atomStringValue.endsWith("\"")) {
            String stringWithoutQuotes = atomStringValue.substring(1, atomStringValue.length()-1);
            return new StringValue(stringWithoutQuotes);
        }
        else {
            // could be in the environment; otherwise fall back to int
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            Optional<Value<?>> possibleValue = environment.get(symbol);
            if(possibleValue.isPresent()) {
                return possibleValue.get();
            }

            int intValue = Integer.parseInt(atomStringValue);
            return new IntegerValue(intValue);
        }
    }
}
