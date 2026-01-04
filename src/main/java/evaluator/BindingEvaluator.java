package evaluator;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.Node;
import value.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BindingEvaluator {
    private static final String AMP_REST = "&rest";

    /**
     * For macro expansion - does not evaluate the binding values, but passes them as literal symbols or other values.
     */
    public Map<Symbol, Value<?>> evaluateWithNodes(List<Atom> bindings,
                                                   List<Node> operands,
                                                   Environment environment) {

        List<Value<?>> operandValues = operands.stream()
                .map(operand -> nodeToValueNoLookup(operand, environment))
                .collect(Collectors.toUnmodifiableList());

        return evaluateWithValues(bindings, operandValues, environment);
    }

    public Map<Symbol, Value<?>> evaluateWithValues(List<Atom> bindings,
                                                    List<Value<?>> operands,
                                                    Environment environment) {
        Map<Symbol, Value<?>> bindingsMap = new HashMap<>();
        for (int i = 0; i < bindings.size(); i++) {
            Atom bindingAtom = bindings.get(i);
            String bindingName = bindingAtom.value();
            if (bindingName.equals(AMP_REST)) {
                // 1. get next binding - this is the name of the list representing the remaining args
                String restArgsBindingName = bindings.get(i + 1).value();
                Symbol restArgsBindingSymbol = environment.internSymbol(restArgsBindingName);

                // 2. get remaining operands - put them all in a list and assign to the symbol
                List<Value<?>> restOperands = operands.subList(i, operands.size());

                // 3. convert to a cons list
                ConsCellValue restValuesCons = ConsCellValue.fromJavaList(restOperands);

                // 4. add this binding to the bindingsMap
                bindingsMap.put(restArgsBindingSymbol, restValuesCons);

                // 5. break out of loop; for now we're treating the &rest binding as the last thing we'd see
                break;
            } else {
                Symbol bindingSymbol = environment.internSymbol(bindingName);
                bindingsMap.put(bindingSymbol, operands.get(i));
            }
        }
        return bindingsMap;
    }

    Value<?> nodeToValueNoLookup(Node node,
                                 Environment environment) {
        if(node instanceof Atom atom) {
            return atomToValueNoLookup(atom, environment);
        }
        else {
            throw new UnsupportedOperationException("lists not yet supported here");
        }
    }

    Value<?> atomToValueNoLookup(Atom atom,
                                 Environment environment) {
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
        else if(isNumeric(atomStringValue)) {
            int intValue = Integer.parseInt(atomStringValue);
            return new IntegerValue(intValue);
        }
        else {
            // treat as symbol
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
