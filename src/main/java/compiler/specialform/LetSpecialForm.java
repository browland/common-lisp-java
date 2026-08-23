package compiler.specialform;

import compiler.treewalker.CompilerBackend;
import compiler.treewalker.TreeWalker;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

public class LetSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        backend.writeComment("let: " + rlist);

        // Get bindings generate code to eval them and put result values onto stack.
        RList bindingsList = RList.expectRList(rlist.nodes().get(1));

        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        int numBindings = bindingsList.size();
        backend.reserveStackForVariables(numBindings);

        // For each binding we eval its value and put it on the stack and record its offset
        int pos = 0;

        backend.pushStackOffsetFrame();

        for (Node binding : bindingsList.nodes()) {
            RList bindingList = RList.expectRList(binding);
            Atom nameAtom = Atom.expectAtom(bindingList.nodes().getFirst());
            Node bindingValue = bindingList.nodes().get(1);

            // Evaluate binding value - after tree-walk for this binding value it'll be in x0.
            treeWalker.walkTree(bindingValue);

            // Push to stack at appropriate offset
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            backend.storeResultingLetBindingToStack(numBindings, pos, nameAtom.value());
            pos++;
        }

        // Evaluate let body
        Node bodyNode = rlist.nodes().get(2);
        treeWalker.walkTree(bodyNode);

        backend.freeStackForVariables(numBindings);
    }
}
