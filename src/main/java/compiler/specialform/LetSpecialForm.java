package compiler.specialform;

import compiler.AsmGenerator;
import compiler.treewalker.Function;
import compiler.treewalker.TreeWalker;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.HashMap;
import java.util.Map;

public class LetSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator, Function currentFunctionScope) {
        asmGenerator.writeComment("let: " + rlist);

        // Get bindings generate code to eval them and put result values onto stack.
        RList bindingsList = RList.expectRList(rlist.nodes().get(1));

        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        int numBindings = bindingsList.size();
        int stackBytes = (int)(16 * Math.ceil(numBindings/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        // For each binding we eval its value and put it on the stack and record its offset
        int pos = 0;
        Map<String, Integer> stackOffsets = new HashMap<>();
        int startOffset = currentFunctionScope.getMinOffset();
        for (Node binding : bindingsList.nodes()) {
            RList bindingList = RList.expectRList(binding);
            Atom nameAtom = Atom.expectAtom(bindingList.nodes().getFirst());
            Node bindingValue = bindingList.nodes().get(1);

            // Evaluate binding value - after tree-walk for this binding value it'll be in x0.
            treeWalker.walkTree(bindingValue, currentFunctionScope);

            // Push to stack at appropriate offset
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            asmGenerator.storeOperandFromRegisterToStack(pos);
            int stackOffset = -1*stackBytes + (pos*8) + startOffset;  // startOffset is negative
            stackOffsets.put(nameAtom.value(), stackOffset);
            pos++;
        }

        currentFunctionScope.pushStackOffsets(stackOffsets);

        // Evaluate let body
        Node bodyNode = rlist.nodes().get(2);
        treeWalker.walkTree(bodyNode, currentFunctionScope);

        asmGenerator.freeSpaceOnStack(stackBytes);
    }
}
