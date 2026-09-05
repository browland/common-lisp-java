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
        // Determine name of function
        Node nameNode = rlist.nodes().get(1);
        SymbolAtom nameSymbolAtom = TypedAtom.toSymbolAtom(nameNode);
        String name = nameSymbolAtom.getValue();

        // put the AsmGenerator into new function scope
        backend.startFunction(name);

        // Bindings
        Node bindingsNode = rlist.nodes().get(2);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();
        int numBindings = bindingsList.size();

        backend.reserveStackForVariables(numBindings);

        // At runtime we expect the caller to have passed the arguments to registers x0, x1, ...
        // For each binding, we push the operand in that register position to the stack and then associate that stack
        // offset with the symbol in the map on the Function.

        // first collect List of binding names in order
        List<String> bindingNameList = new ArrayList<>();
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            bindingNameList.add(symbolAtom.value());
        }

        // This is where we register the Function
        backend.setUpFunctionBindings(name, bindingNameList);

        // Function impl
        Node bodyNode = rlist.nodes().get(3);
        treeWalker.walkTree(bodyNode);

        Function function = backend.findFunction(name);
        backend.endFunction(function);
    }
}
