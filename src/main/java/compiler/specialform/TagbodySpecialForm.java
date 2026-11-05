package compiler.specialform;

import compiler.treewalker.CompilerBackend;
import compiler.treewalker.SymbolAtom;
import compiler.treewalker.TreeWalker;
import compiler.treewalker.TypedAtom;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.*;

public class TagbodySpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        // TODO Each tag has its own lexical scope, e.g. setf of variable bound outside won't affect the outer scope.
        // TODO tagbodies can be nested.  So each tag works just like let in that regard.
        // TODO We can 'go' to tags after the point we're at, so we need to collect the tags (any atoms) in an initial pass.
        // TODO When we encounter 'go' it's only valid if we're still within a tagbody containing it (hasn't been exited) -
        // TODO track tags at current tagbody level (with nesting) so we can validate if a 'go' is valid (tag is within current lexical scope).

        // First get all tags - these are just the atoms at top level, ignoring the initial 'tagbody' node.
        // We need this as we might encounter a 'go' during evaluation of forms, where the tag has not appeared yet.
        // For now we'll just treat symbols as valid go tags, e.g. not numbers.  We'll ignore any other atoms.
        Set<String> tagNames = new HashSet<>();
        for (Node node : rlist.nodes().subList(1, rlist.nodes().size())) {
            if (node instanceof Atom tagAtom) {
                TypedAtom<?> typedTagAtom = TypedAtom.fromAtom(tagAtom);
                if (typedTagAtom instanceof SymbolAtom symbolTagAtom) {
                    if (tagNames.contains(symbolTagAtom.getValue())) {
                        throw new RuntimeException("Duplicate tag found in tagbody: " + rlist);
                    }
                    tagNames.add(symbolTagAtom.getValue());
                }
            }
        }

        Map<String,String> tagToLabelName = backend.enterTagbody(tagNames);

        // Now evaluate each element of the tagbody.  If we see an atom we emit the label given to us in tagToLabelName.
        // If we see a list we generate the code for it.
        for (Node node : rlist.nodes().subList(1, rlist.nodes().size())) {
            if (node instanceof Atom tagAtom) {
                TypedAtom<?> typedTagAtom = TypedAtom.fromAtom(tagAtom);
                if (typedTagAtom instanceof SymbolAtom symbolTagAtom) {
                    backend.generateLabel(tagToLabelName.get(symbolTagAtom.getValue()));
                }
            }
            else {
                treeWalker.walkTree(node);
            }
        }

        backend.leaveTagbody();
    }
}
