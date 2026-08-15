package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import compiler.treewalker.Function;
import compiler.treewalker.SymbolAtom;
import compiler.treewalker.TreeWalker;
import compiler.treewalker.TypedAtom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefunSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator, Function currentFunctionScope) {
        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();

        Node nameNode = rlist.nodes().get(1);
        SymbolAtom nameSymbolAtom = TypedAtom.toSymbolAtom(nameNode);
        String name = nameSymbolAtom.getValue();

        // Generate the global variable for the symbol for this function name
//        asmGenerator.generateDataSectionQuadWordForSymbolPtr(name);
//        asmGenerator.generateCStringForSymbol(name);
        asmGenerator.addToSymbolTable(name);

        asmGenerator.initFunction(name);

        // Bindings
        Node bindingsNode = rlist.nodes().get(2);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();
        int numBindings = bindingsList.size();

        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        int stackBytes = (int)(16 * Math.ceil(numBindings/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        // At runtime we expect the caller to have passed the arguments to registers x0, x1, ...
        // For each binding, we push the operand in that register position to the stack and then associate that stack
        // offset with the symbol in the map on the Function.
        int pos = 0;
        Map<String, Integer> stackOffsets = new HashMap<>();
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            System.out.printf("defun: storing binding for %s to stack%n", symbolAtom.value());
            asmGenerator.storeOperandFromRegisterToStack(pos);
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            int stackOffset = -1*stackBytes + (pos*8);
            stackOffsets.put(symbolAtom.value(), stackOffset);
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
    }
}
