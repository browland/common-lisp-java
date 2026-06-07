package evaluator;

import evaluator.env.Symbols;
import exception.EvaluationException;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
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
    public Map<Symbol, Value<?>> assignBindingsFromNodeOperands(List<Node> bindings,
                                                                List<Node> operands) {

        List<Value<?>> operandValues = operands.stream()
                .map(this::nodeToValueNoLookup)
                .collect(Collectors.toUnmodifiableList());

        return assignBindingsFromValueOperands(bindings, operandValues);
    }

    public Map<Symbol, Value<?>> assignBindingsFromValueOperands(List<Node> bindings,
                                                                 List<Value<?>> operands) {
        boolean restMode = false;

        Map<Symbol, Value<?>> bindingsMap = new HashMap<>();
        for (int i = 0; i < bindings.size(); i++) {
            Node currentBindingNode = bindings.get(i);
            BindingType bindingType = bindingType(currentBindingNode);
            if(BindingType.REST == bindingType) {
                restMode = true;
                bindingsMap.putAll(collectRestBinding(bindings, operands, i));
                break;  // don't consume the last atom; we already handled it along with &rest
            }
            else if(BindingType.ATOM == bindingType) {
                bindingsMap.putAll(collectAtomBinding(bindings, operands, i));
            }
            else if(BindingType.LIST == bindingType) {
                if(operands.size() < i+1) {
                    throw new EvaluationException("Expecting list parameter in position " + (i+1));
                }
                bindingsMap.putAll(collectListBinding((RList)currentBindingNode, (ConsCellValue)operands.get(i)));
            }
        }

        if(restMode) {
            if(operands.size() < bindings.size()-1) {
                throw new EvaluationException("Expected at least " + (bindings.size() - 1) + " arguments but got " + operands.size());
            }
        }
        else {
            if (operands.size() != bindings.size()) {
                throw new EvaluationException("Expected  " + (bindings.size()) + " arguments but got " + operands.size());
            }
        }

        return bindingsMap;
    }

    private Map<Symbol,Value<?>> collectListBinding(RList listBindings,
                                                    ConsCellValue consCellValueOperand) {
        Map<Symbol, Value<?>> bindingsMap = new HashMap<>();

        ConsCell currentCons = consCellValueOperand.getValue();
        Value<?> car = currentCons.car();
        Value<?> cdr = currentCons.cdr();

        // todo for now assuming only atoms in the list (destructuring only one level deep

        for(Node bindingNode : listBindings.nodes()) {
            Atom currentBindingAtom = (Atom)bindingNode;

            String bindingName = currentBindingAtom.value();
            Symbol bindingSymbol = Symbols.internSymbol(bindingName);

            bindingsMap.put(bindingSymbol, car);

            if(cdr.getType() == ValueType.CONS_CELL) {
                currentCons = ((ConsCellValue)cdr).getValue();
                car = currentCons.car();
                cdr = currentCons.cdr();
            }
            else {
                // we should have no more iterations and cdr should be nil - check this
                if(cdr.getValue() != Symbols.nil()) {
                    throw new IllegalArgumentException("unusual cons with cdr not a ConsCellValue nor nil");
                }
            }
        }

        return bindingsMap;
    }

    private BindingType bindingType(Node currentNode) {
        if(currentNode instanceof RList) {
            return BindingType.LIST;
        }
        else if(currentNode instanceof Atom atom) {
            if(AMP_REST.equals(atom.value())) {
                return BindingType.REST;
            }
            return BindingType.ATOM;
        }
        throw new UnsupportedOperationException("Unhandled binding type for " + currentNode);
    }

    private Map<Symbol,Value<?>> collectRestBinding(List<Node> bindings,
                                                    List<Value<?>> operands,
                                                    int currentIndex) {
        Map<Symbol,Value<?>> bindingSubsetMap = new HashMap<>();

        // 0. validation - we expect an atom as the next binding after &rest
        if(!(bindings.get(currentIndex + 1) instanceof Atom restArgsBindingAtom)) {
            throw new IllegalArgumentException("must provide atom after &rest in bindings");
        }


        // 1. get next binding - this is the name of the list representing the remaining args
        String restArgsBindingName = restArgsBindingAtom.value();
        Symbol restArgsBindingSymbol = Symbols.internSymbol(restArgsBindingName);

        // 2. get remaining operands - put them all in a list and assign to the symbol
        List<Value<?>> restOperands = operands.subList(currentIndex, operands.size());

        // 3. convert to a cons list
        if(restOperands.isEmpty()) {
            throw new EvaluationException("Expected at least one argument for &rest binding");
        }
        ConsCellValue restValuesCons = ConsCellValue.fromJavaList(restOperands);

        // 4. add this binding to the bindingsMap
        bindingSubsetMap.put(restArgsBindingSymbol, restValuesCons);

        // 5. break out of loop; for now we're treating the &rest binding as the last thing we'd see
        return bindingSubsetMap;
    }

    private Map<Symbol,Value<?>> collectAtomBinding(List<Node> bindings,
                                                    List<Value<?>> operands,
                                                    int currentIndex) {
        Map<Symbol,Value<?>> bindingSubsetMap = new HashMap<>();
        Atom currentBindingAtom = (Atom)bindings.get(currentIndex);

        String bindingName = currentBindingAtom.value();
        Symbol bindingSymbol = Symbols.internSymbol(bindingName);

        if(operands.size() < currentIndex+1) {
            throw new EvaluationException(String.format("Expected %d bindings, got only %d", bindings.size(), operands.size()));
        }
        bindingSubsetMap.put(bindingSymbol, operands.get(currentIndex));

        return bindingSubsetMap;
    }

    Value<?> nodeToValueNoLookup(Node node) {
        if(node instanceof Atom atom) {
            return atomEvaluator.atomToValueNoLookup(atom.value());
        }
        else {
            return ConsCellValue.fromRList((RList)node);
        }
    }


    enum BindingType {
        REST, ATOM, LIST
    }
}
