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

    private final AtomEvaluator atomEvaluator = new AtomEvaluator();

    /**
     * For macro expansion - does not evaluate the binding values, but passes them as literal symbols or other values.
     */
    public Map<Symbol, Value<?>> evaluateWithNodes(List<Atom> bindings,
                                                   List<Node> operands,
                                                   Environment environment) {

        List<Value<?>> operandValues = operands.stream()
                .map(this::nodeToValueNoLookup)
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
                Symbol restArgsBindingSymbol = Symbols.internSymbol(restArgsBindingName);

                // 2. get remaining operands - put them all in a list and assign to the symbol
                List<Value<?>> restOperands = operands.subList(i, operands.size());

                // 3. convert to a cons list
                ConsCellValue restValuesCons = ConsCellValue.fromJavaList(restOperands);

                // 4. add this binding to the bindingsMap
                bindingsMap.put(restArgsBindingSymbol, restValuesCons);

                // 5. break out of loop; for now we're treating the &rest binding as the last thing we'd see
                break;
            } else {
                Symbol bindingSymbol = Symbols.internSymbol(bindingName);
                bindingsMap.put(bindingSymbol, operands.get(i));
            }
        }
        return bindingsMap;
    }

    Value<?> nodeToValueNoLookup(Node node) {
        if(node instanceof Atom atom) {
            return atomEvaluator.atomToValueNoLookup(atom.value());
        }
        else {
            throw new UnsupportedOperationException("lists not yet supported here");
        }
    }
}
