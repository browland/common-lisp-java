package compiler.specialform;

import compiler.Namespace;
import compiler.treewalker.*;
import syntaxtree.Atom;
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
    }
}
