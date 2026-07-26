package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.Node;
import syntaxtree.RList;
import treewalker.SymbolAtom;
import treewalker.TreeWalker;
import treewalker.TypedAtom;

public class DefunSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator) {
        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();

        Node nameNode = rlist.nodes().get(1);
        SymbolAtom nameSymbolAtom = TypedAtom.toSymbolAtom(nameNode);
        String name = nameSymbolAtom.getValue();
        asmGenerator.initFunction(name);

        // Function impl
        Node bodyNode = rlist.nodes().get(2);
        treeWalker.walkTree(bodyNode);

        asmGenerator.endFunction();

        // return the AsmGenerator from new function scope
        // TODO confusing how this differs from endFunction() - doing different things
        asmGenerator.endFunctionDef();

        // associate symbol for this function with the tagged function ptr for it
        asmGenerator.generateCStringForSymbol(name);

        asmGenerator.putFunction(name);
    }
}
