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
        String program = "\"hello world\"";
        List<Node> nodes = nodeBuilder.build(program);
        walker.walkTree(nodes);

        // int atom
        program = "1";
        nodes = nodeBuilder.build(program);
        walker.walkTree(nodes);

        // float atom
        program = "1.23";
        nodes = nodeBuilder.build(program);
        walker.walkTree(nodes);

        // char atom
        program = "#\\c";
        nodes = nodeBuilder.build(program);
        walker.walkTree(nodes);
    }

    // We walk through each node in turn at this level, recursing for any list encountered in any of the Node positions.
    // Once we end up with the evaluated list of nodes, then we evaluate the "flattened" list at this level as a form.
    // We call into our NodeListener for individual atoms as well as the overall form at each level while we still
    // evolve the design.
    private void walkTree(List<Node> nodes) {
        List<TypedAtom<?>> evaluatedNodes = new ArrayList<>();
        for (Node node : nodes) {
            if (node instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
                evaluatedNodes.add(typedAtom);
                nodeListener.handleAtom(typedAtom);
            }
            else if (node instanceof RList rlist) {
                walkTree(rlist.nodes());
            }
        }

        nodeListener.handleForm(evaluatedNodes);
    }
}
