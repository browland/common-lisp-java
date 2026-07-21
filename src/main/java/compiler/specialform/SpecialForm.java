package compiler.specialform;

import syntaxtree.RList;
import treewalker.TreeWalker;

import java.io.IOException;

public interface SpecialForm {
    // TODO we should have TreeWalker as a field and it should ideally be stateless, so we can just call it from anywhere to generate asm.
    //      We'll surely need some kind of context to keep track of things like stack usage; but TreeWalker isn't the place for it.
    void walkTree(RList rlist, TreeWalker treeWalker) throws IOException;
}
