package compiler.specialform;

import compiler.treewalker.*;
import syntaxtree.Node;
import syntaxtree.RList;

/*
 We initialise the symbol table entry with null in the data namespace slot.
 At runtime, the expression will be evaluated and written to the data namespace slot.
 Arguably we could write the data slot statically if we know it's being set to a compile-time constant (e.g. a fixnum)
 but that could be done later.
 */
public class DefvarSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        Node symbolNode = rlist.get(1);
        SymbolAtom symbolAtom = TypedAtom.toSymbolAtom(symbolNode);
        String symbolValue = symbolAtom.getValue();

        // Record that this symbol is associated with a value in the data namespace
        backend.recordVariableExists(symbolValue);

        // Generate the global variable for this symbol
        boolean symbolCreated = backend.initialiseSymbol(symbolValue);
        if (!symbolCreated) {
            throw new IllegalArgumentException("Trying to re-create symbol table entry for " + symbolValue);
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
