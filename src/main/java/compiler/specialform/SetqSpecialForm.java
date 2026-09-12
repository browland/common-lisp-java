package compiler.specialform;

import compiler.treewalker.CompilerBackend;
import compiler.treewalker.SymbolAtom;
import compiler.treewalker.TreeWalker;
import compiler.treewalker.TypedAtom;
import syntaxtree.Node;
import syntaxtree.RList;

public class SetqSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        Node symbolNode = rlist.get(1);
        SymbolAtom symbolAtom = TypedAtom.toSymbolAtom(symbolNode);
        String symbolValue = symbolAtom.getValue();

        // Check variable was already defined
        if (!backend.getDeclaredVariableNames().contains(symbolValue)) {
            throw new IllegalArgumentException("setq: variable was not defined");
        }

        // Reserve space on stack - pretend 2 variables to get multiple of 16 bytes
        backend.reserveStackForVariables(2);

        // Evaluate value node; the result will end up in x0
        Node valueNode = rlist.nodes().get(2);
        treeWalker.walkTree(valueNode);

        // value is now in x0, so update value of symbol
        backend.storeResultToSymbolValue(symbolValue);

        // Free space on stack - pretend 2 variables to get multiple of 16 bytes for aarch64
        backend.freeStackForVariables(2);
    }
}
