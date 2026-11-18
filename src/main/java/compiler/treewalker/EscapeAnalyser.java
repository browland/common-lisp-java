package compiler.treewalker;

import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EscapeAnalyser {
    public List<String> findFreeVariables(List<String> bindingsList, Set<String> declaredFunctionNames,
                                          Set<String> declaredSpecialForms, Set<String> declaredMacroNames,
                                          Set<String> declaredVariableNames, RList lambdaBody) {
        return walkTree(bindingsList, declaredFunctionNames, declaredVariableNames, declaredSpecialForms, declaredMacroNames, lambdaBody);
    }

    private List<String> walkTree(List<String> bindingsList, Set<String> declaredFunctionNames,
                                  Set<String> declaredSpecialForms, Set<String> declaredMacroNames,
                                  Set<String> declaredVariableNames, RList lambdaBody) {
        List<String> referencedVariables = new ArrayList<>();

        // If this is a quoted list then we treat it as data and there are no captured variables anywhere within the tree
        // at this level.
        Node firstNode = lambdaBody.nodes().getFirst();
        if (firstNode instanceof Atom operatorAtom) {
            if ("quote".equals(operatorAtom.value())) {
                return referencedVariables;
            }
        }

        for (Node node : lambdaBody.nodes()) {
            if (node instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypeCoercer.coerceType(atom);
                if (typedAtom instanceof SymbolAtom symbolAtom) {
                    if (isFreeVariable(symbolAtom.getValue(), bindingsList, declaredFunctionNames, declaredSpecialForms, declaredMacroNames, declaredVariableNames)) {
                        referencedVariables.add(atom.value());
                    }
                }
            }
            else if (node instanceof RList rlist){
                referencedVariables.addAll(walkTree(bindingsList, declaredFunctionNames, declaredSpecialForms, declaredMacroNames, declaredVariableNames, rlist));
            }
        }
        return referencedVariables;
    }

    private boolean isFreeVariable(String symbol, List<String> bindingsList, Set<String> declaredFunctionNames,
                                   Set<String> declaredSpecialForms, Set<String> declaredMacroNames, Set<String> declaredVariableNames) {
        return !(bindingsList.contains(symbol) || declaredFunctionNames.contains(symbol) || declaredSpecialForms.contains(symbol)
                || declaredVariableNames.contains(symbol) || declaredMacroNames.contains(symbol));
    }
}
