package treewalker;

import reader.NodeBuilder;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.ArrayList;
import java.util.List;

// Beginnings of compiler and might end up being retro-fitted to the existing interpreter as a general case of tree-
// walking.
public class TreeWalker {
    private final NodeListener nodeListener = new NodeHandler();

    public static void main(String[] args) {
        NodeBuilder nodeBuilder = new NodeBuilder();

        TreeWalker walker = new TreeWalker();

        // String atom
        String program = "(+ 1 (+ 1 2))";
        List<Node> nodes = nodeBuilder.build(program);
        walker.walkTopLevelNodes(nodes);
    }

    // We walk through each node in turn at this level, recursing for any list encountered in any of the Node positions.
    // Once we end up with the evaluated list of nodes, then we evaluate the "flattened" list at this level as a form.
    // We call into our NodeListener for individual atoms as well as the overall form at each level while we still
    // evolve the design.
    private void walkTopLevelNodes(List<Node> nodes) {
        for (Node node : nodes) {
            walkTree(node);
        }
    }

    private void walkTree(Node node) {
        if (node instanceof Atom atom) {
            handleAtom(atom);
        }
        else if (node instanceof RList rlist) {
            walkTree(rlist);
        }
    }

    private TypedAtom<?> handleAtom(Atom atom) {
        TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
        nodeListener.handleAtom(typedAtom);
        return typedAtom;
    }

    private void walkTree(RList rlist) {
        // This is a form.  When we first begin a form we need to call down to NodeListener as that maps to creation of
        // a function/stack frame.
        nodeListener.startForm();

        List<TypedAtom<?>> handledNodes = new ArrayList<>();
        for (Node childNode : rlist.nodes()) {
            if (childNode instanceof Atom atom) {
                TypedAtom<?> typedAtom = handleAtom(atom);
                handledNodes.add(typedAtom);
            }
            else if (childNode instanceof RList innerRList) {
                // todo we need some kind of r.v. to represent the processing of the inner form, e.g. another TypedNode
                //      which we can then add to the list of handledNodes at this level
                //      Otherwise we can't generate code to call the inner function, though we could perhaps assume there'll
                //      be an r.v. of some kind we can use for this place in the form at this level.
                //      E.g. a FormTypedNode which holds its TypedNode list and stores the name of its generated function.
                walkTree(innerRList);
            }
        }

        nodeListener.applyForm(handledNodes);
    }

}
