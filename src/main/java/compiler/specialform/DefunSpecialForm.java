package compiler.specialform;

import compiler.treewalker.*;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.ArrayList;
import java.util.List;

public class DefunSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        // put the AsmGenerator into new function scope
        backend.startFunctionDefinition();

        Node nameNode = rlist.nodes().get(1);
        SymbolAtom nameSymbolAtom = TypedAtom.toSymbolAtom(nameNode);
        String name = nameSymbolAtom.getValue();

        backend.initialiseSymbol(name);
        backend.functionPrologue(name);  // TODO we allocate 16 bytes on stack here

        // Bindings
        Node bindingsNode = rlist.nodes().get(2);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();
        int numBindings = bindingsList.size();

        backend.reserveStackForVariables(numBindings);  // TODO we allocate 0 bytes on stack here

        // At runtime we expect the caller to have passed the arguments to registers x0, x1, ...
        // For each binding, we push the operand in that register position to the stack and then associate that stack
        // offset with the symbol in the map on the Function.

        // first collect List of binding names in order
        List<String> bindingNameList = new ArrayList<>();
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            bindingNameList.add(symbolAtom.value());
        }

        backend.setUpFunctionBindings(name, bindingNameList);

        // Function impl
        Node bodyNode = rlist.nodes().get(3);
        treeWalker.walkTree(bodyNode);

        backend.leaveFunction(name, numBindings);  // TODO we free 16 bytes of stack here (for x29 and x30)
    }
}
