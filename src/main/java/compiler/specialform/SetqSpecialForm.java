package compiler.specialform;

import compiler.treewalker.CompilerBackend;
import compiler.treewalker.SymbolAtom;
import compiler.treewalker.TreeWalker;
import compiler.treewalker.TypedAtom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.Optional;

public class SetqSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        Node symbolNode = rlist.get(1);
        SymbolAtom symbolAtom = TypedAtom.toSymbolAtom(symbolNode);
        String symbolValue = symbolAtom.getValue();

        // Step 1: Look for symbol in current lexical scope
        Optional<Integer> optionalOffset = backend.findOffsetInLexicalScope(symbolValue);
        boolean isLexicallyBound = optionalOffset.isPresent();

        boolean isGlobalVariable = backend.getDeclaredVariableNames().contains(symbolValue);

        // Step 2: Check symbol is bound to a global variable
        if (!isLexicallyBound && !isGlobalVariable) {
            throw new IllegalArgumentException("setq: symbol is not bound");
        }

        // Reserve space on stack - pretend 2 variables to get multiple of 16 bytes
        backend.reserveStackForVariables(2);

        // Evaluate value node; the result will end up in x0
        Node valueNode = rlist.nodes().get(2);
        treeWalker.walkTree(valueNode);

        // value is now in x0, so update value of symbol
        if (isGlobalVariable) {
            backend.storeResultToSymbolValue(symbolValue);
        }
        else {
            // lexically bound
            backend.storeResultToStackFPOffset(optionalOffset.get());
        }

        // Free space on stack - pretend 2 variables to get multiple of 16 bytes for aarch64
        backend.freeStackForVariables(2);
    }
}
