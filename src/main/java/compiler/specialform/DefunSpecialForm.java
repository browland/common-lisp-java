package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import treewalker.Function;
import treewalker.SymbolAtom;
import treewalker.TreeWalker;
import treewalker.TypedAtom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefunSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator, Function currentFunction) {
        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();

        Node nameNode = rlist.nodes().get(1);
        SymbolAtom nameSymbolAtom = TypedAtom.toSymbolAtom(nameNode);
        String name = nameSymbolAtom.getValue();

        // Generate the global variable for this symbol
        asmGenerator.generateDataSectionQuadWordForSymbolPtr(name);
        asmGenerator.generateCStringForSymbol(name);

        asmGenerator.initFunction(name);

        // Bindings
        Node bindingsNode = rlist.nodes().get(2);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();
        int numBindings = bindingsList.size();

        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        int stackBytes = (int)(16 * Math.ceil(numBindings/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        // TODO think about bindings and scopes ...
        //      Generate code to (i) create new symbol table scope (linked list), then copy each runtime arg (from register)
        //      into matching symbol in that new scope.  E.g. if bindings are (x,y,z) then we set x to x0, y to x1 etc.
        // TODO for now just add into the existing symbol table blindly
        //      e.g. put_symbol from each register for each binding
        //      But we have to first put each arg onto the stack so they're not still in calling convention position, so we can
        //      call into our runtime to add each symbol mapping one-by-one.
        int pos = 0;
        Map<String, Integer> stackOffsets = new HashMap<>();
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            System.out.printf("defun: storing binding for %s to stack%n", symbolAtom.value());
            asmGenerator.storeOperandFromRegisterToStack(pos);
            // Stack offset is relative to the frame pointer; values will be {-8, -16, ...}.
            stackOffsets.put(symbolAtom.value(), (pos+1)*-8);
            pos++;
        }

        // Store stack offsets on Function so we can pass them around with relevant context
        Function function = new Function(name, stackOffsets);

        // Function impl
        Node bodyNode = rlist.nodes().get(3);
        treeWalker.walkTree(bodyNode, function);

        // We need to free an additional 16 bytes for the stored x29 and x30 regs
        asmGenerator.freeSpaceOnStack(stackBytes + 16);

        asmGenerator.endFunction();

        // return the AsmGenerator from new function scope
        // TODO confusing how this differs from endFunction() - doing different things
        asmGenerator.endFunctionDef();
        asmGenerator.putFunction(name);

        // add this function to our compile-time Map
        treeWalker.getFunctions().put(name, function);

        // TODO update Function with stack offsets of our bindings, then pass it around
    }
}
