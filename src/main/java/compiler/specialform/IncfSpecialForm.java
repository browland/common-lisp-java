package compiler.specialform;

import compiler.Namespace;
import compiler.treewalker.*;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.Optional;

/**
 * Very similar to setq so is based off that code.
 */
public class IncfSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        Node symbolNode = rlist.get(1);
        SymbolAtom symbolAtom = TypedAtom.toSymbolAtom(symbolNode);
        String symbolValue = symbolAtom.getValue();

        // Step 1: Look for symbol in current lexical scope or as global
        Optional<Integer> optionalOffset = backend.findOffsetInLexicalScope(symbolValue);
        boolean isLexicallyBound = optionalOffset.isPresent();
        boolean isGlobalVariable = backend.getDeclaredVariableNames().contains(symbolValue);

        // Step 2: Check symbol exists somehow
        if (!isLexicallyBound && !isGlobalVariable) {
            throw new IllegalArgumentException("incf: symbol is not bound");
        }

        // If global var then load pointer to start of symbol entry
        if (isGlobalVariable) {
            backend.handleSymbolOperandNoLookup(symbolValue);
            backend.callFunction("_inc_global");
        }
        else {
            // We need to increment the fixnum at the stack offset, bearing in mind that fixnums have 3 low bits of type tag
            // (so increment by 8).
            int offset = optionalOffset.get();
            backend.writeAsm("""
                    ldr x0, [fp, #%d]
                    add x0, x0, #8
                    str x0, [fp, #%d]
                    """.formatted(offset, offset));
        }

        // Load resulting value of symbol (works for either binding or global)
        backend.handleSymbolOperand(symbolValue, Namespace.VARIABLE);
    }
}
