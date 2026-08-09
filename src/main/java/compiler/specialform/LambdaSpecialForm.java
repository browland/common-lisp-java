package compiler.specialform;

import compiler.AsmGenerator;
import compiler.treewalker.Function;
import compiler.treewalker.TreeWalker;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LambdaSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator, Function currentFunctionScope) {
        // TODO: Generate function in asm, similar to defun.  Name dynamically generated and not exposed anywhere at user program level.
        //       Allocate closure struct with generated function name, and ptr to heap where captured vars are copied to.
        //       We then generate a tagged ptr to this struct as the evaluated value of this position.
        //       Ensure we know how to follow and handle this tagged ptr when the call is made.
        //       The pointer tagging ensures we can decouple lambda evaluation from application (just another value we pass around).
        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();

        // TODO
        String name = "closure_0";

        // Generate the global variable for this symbol
        asmGenerator.generateDataSectionQuadWordForSymbolPtr(name);
        asmGenerator.generateCStringForSymbol(name);

        asmGenerator.initFunction(name);

        // Bindings
        Node bindingsNode = rlist.nodes().get(1);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();
        int numBindings = bindingsList.size();

        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        final int stackBytes = (int)(16 * Math.ceil(numBindings/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        int pos = 0;
        Map<String, Integer> stackOffsets = new HashMap<>();
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            System.out.printf("lambda: storing binding for %s to stack%n", symbolAtom.value());
            asmGenerator.storeOperandFromRegisterToStack(pos);
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            int stackOffset = -1*stackBytes + (pos*8);
            stackOffsets.put(symbolAtom.value(), stackOffset);
            pos++;
        }

        // Store stack offsets on Function so we can pass them around with relevant context
        Function function = new Function(name, stackOffsets);

        // Function impl
        Node bodyNode = rlist.nodes().get(2);
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
