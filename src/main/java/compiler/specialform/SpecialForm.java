package compiler.specialform;

import compiler.AsmGenerator;
import treewalker.Function;
import syntaxtree.RList;
import treewalker.TreeWalker;

public interface SpecialForm {
    // TODO we should have TreeWalker as a field and it should ideally be stateless, so we can just call it from anywhere to generate asm.
    //      We'll surely need some kind of context to keep track of things like stack usage; but TreeWalker isn't the place for it.
    void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator, Function currentFunction);
}
