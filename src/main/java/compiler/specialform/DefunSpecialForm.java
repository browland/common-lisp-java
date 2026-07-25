package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.RList;
import treewalker.TreeWalker;

public class DefunSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator) {
        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();
        // return the AsmGenerator from new function scope
        asmGenerator.endFunctionDef();
    }
}
