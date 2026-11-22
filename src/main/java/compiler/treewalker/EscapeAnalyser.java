package compiler.treewalker;

import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EscapeAnalyser {
    public List<String> findFreeVariables(List<String> bindingsList, Set<String> declaredFunctionNames,
                                          Set<String> declaredSpecialForms, Set<String> declaredMacroNames,
                                          Set<String> declaredVariableNames, List<Node> lambdaBodyNodes, CompilerBackend backend) {
        List<String> possiblyCapturedVariables = walkTree(bindingsList, declaredFunctionNames, declaredVariableNames, declaredSpecialForms, declaredMacroNames, lambdaBodyNodes);

        // Remove any variables which aren't in the enclosing scope; possibly defined in inner let bindings
        // TODO needs sorting out properly - this avoids issue where we define let bindings within a closure.  But this will break if we capture a variable and we then shadow
        //      it in an inner let, and use both of them.  Needs a richer method for escape analysis - it's not just a list of symbol names; it's where they're used, too.
        List<String> prunedCaptures = possiblyCapturedVariables.stream()
                .filter(variable -> backend.findOffsetInLexicalScope(variable).isPresent())
                .toList();
        Set<String> encounteredCaptures = new HashSet<>();
        List<String> uniqueCaptures = new ArrayList<>();
        for (String capture : prunedCaptures) {
            if (!encounteredCaptures.contains(capture)) {
                uniqueCaptures.add(capture);
                encounteredCaptures.add(capture);
            }
        }
        return uniqueCaptures;
    }

    private List<String> walkTree(List<String> bindingsList, Set<String> declaredFunctionNames,
                                  Set<String> declaredSpecialForms, Set<String> declaredMacroNames,
                                  Set<String> declaredVariableNames, List<Node> lambdaBodyNodes) {
        List<String> referencedVariables = new ArrayList<>();

        for (Node lambdaBody : lambdaBodyNodes) {
            if (lambdaBody instanceof RList lambdaBodyRlist) {
                // If this is a quoted list then we treat it as data and there are no captured variables anywhere within the tree
                // at this level.
                Node firstNode = lambdaBodyRlist.nodes().getFirst();
                if (firstNode instanceof Atom operatorAtom) {
                    if ("quote".equals(operatorAtom.value())) {
                        return referencedVariables;
                    }
                }

                for (Node node : lambdaBodyRlist.nodes()) {
                    if (node instanceof Atom atom) {
                        TypedAtom<?> typedAtom = TypeCoercer.coerceType(atom);
                        if (typedAtom instanceof SymbolAtom symbolAtom) {
                            if (isFreeVariable(symbolAtom.getValue(), bindingsList, declaredFunctionNames, declaredSpecialForms, declaredMacroNames, declaredVariableNames)) {
                                referencedVariables.add(atom.value());
                            }
                        }
                    }
                    else if (node instanceof RList rlist){
                        referencedVariables.addAll(walkTree(bindingsList, declaredFunctionNames, declaredSpecialForms, declaredMacroNames, declaredVariableNames, List.of(rlist)));
                    }
                }
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
