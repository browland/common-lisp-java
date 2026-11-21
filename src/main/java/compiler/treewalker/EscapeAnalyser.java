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
                                          Set<String> declaredVariableNames, RList lambdaBody, CompilerBackend backend) {
        List<String> possiblyCapturedVariables = walkTree(bindingsList, declaredFunctionNames, declaredVariableNames, declaredSpecialForms, declaredMacroNames, lambdaBody);

        // Remove any variables which aren't in the enclosing scope; possibly defined in inner let bindings
        // TODO needs sorting out properly - this avoids issue where we define let bindings within a closure.  But this will break if we capture a variable and we then shadow
        //      it in an inner let, and use both of them.  Needs a richer method for escape analysis - it's not just a list of symbol names; it's where they're used, too.
        List<String> prunedCaptures = possiblyCapturedVariables.stream()
                .filter(variable -> backend.findOffsetInLexicalScope(variable).isPresent())
                .toList();
        return prunedCaptures;
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
