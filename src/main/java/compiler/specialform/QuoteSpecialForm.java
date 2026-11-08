package compiler.specialform;

import compiler.Namespace;
import compiler.treewalker.*;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

public class QuoteSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        if(rlist.nodes().get(1) instanceof Atom atom) {
            TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
            switch(typedAtom) {
                case SymbolAtom s:
                    String symString = s.getValue();
                    backend.handleSymbolOperand(symString, Namespace.VARIABLE);
                    break;
                case IntAtom i:
                    backend.handleIntOperand(i);
                    break;
                default:
                    break;
            }
        }
        else if (rlist.nodes().get(1) instanceof RList quotedRlist) {
            int lenList = quotedRlist.size();

            // variadic args based on no. of nodes in this quoted list - use similar code to TreeWalker:223
            // 1. Reserve space on stack for all quoted list elements
            backend.reserveStackForVariables(lenList);

            // 2. Move each arg to x0, then to apt. stack pos
            for (int pos = 0; pos<lenList; pos++) {
                Node node = quotedRlist.nodes().get(pos);

                if (node instanceof Atom atom) {
                    TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
                    if (typedAtom instanceof IntAtom intAtom) {
                        backend.handleIntOperand(intAtom);
                        backend.storeResultToStack(pos);
                    }
                    else if (typedAtom instanceof SymbolAtom symbolAtom) {
                        // Does a no-op if symbol table entry already exists
                        backend.initialiseSymbol(symbolAtom.getValue(), false);
                        backend.handleSymbolOperandNoLookup(symbolAtom.getValue());
                        backend.storeResultToStack(pos);
                    }
                }
            }

            // 3. Move start address of args on stack to x0
            backend.moveStackPointerToRegister(0, 0);

            // 4. Move length of args to x1
            backend.writeLongToRegister(lenList, 1);

            // 5. call list() C function
            backend.callFunction("_list");

            // 6. clean up stack
            backend.freeStackForVariables(lenList);
        }
    }
}
