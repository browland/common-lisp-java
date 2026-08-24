package compiler.treewalker;

import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class EscapeAnalyser {
    public List<String> findFreeVariables(List<String> bindingsList, Set<String> declaredFunctionNames, RList lambdaBody) {
        return walkTree(bindingsList, declaredFunctionNames, lambdaBody);
    }

    private List<String> walkTree(List<String> bindingsList, Set<String> declaredFunctionNames, RList lambdaBody) {
        List<String> referencedVariables = new ArrayList<>();

        for (Node node : lambdaBody.nodes()) {
            if (node instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypeCoercer.coerceType(atom);
                if (typedAtom instanceof SymbolAtom symbolAtom) {
                    if (isFreeVariable(symbolAtom.getValue(), bindingsList, declaredFunctionNames)) {
                        referencedVariables.add(atom.value());
                    }
                }
            }
            else if (node instanceof RList rlist){
                referencedVariables.addAll(walkTree(bindingsList, declaredFunctionNames, rlist));
            }
        }
        return referencedVariables;
    }

    private boolean isFreeVariable(String symbol, List<String> bindingsList, Set<String> declaredFunctionNames) {
        return !(bindingsList.contains(symbol) || declaredFunctionNames.contains(symbol));
    }
}
