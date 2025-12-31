package evaluator.macro;

import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.NodeBuilder;
import syntaxtree.RList;
import value.Macro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MacroExpander {
    public RList expand(Macro macro,
                        RList entireList) {

        List<Atom> bindings = macro.getBindings();
        RList bodyTemplate = macro.getBody();

        Map<String, Node> bindingsMap = prepareBindings(entireList, bindings);

        NodeBuilder transformedBodyBuilder = expand(bodyTemplate, bindingsMap, false);
        return (RList) transformedBodyBuilder.build();
    }

    private Map<String, Node> prepareBindings(RList entireList, List<Atom> bindings) {
        Map<String, Node> boundArgs = new HashMap<>();
        List<Node> operandNodes = entireList.nodes().subList(1, entireList.size());
        for (int i = 0; i < operandNodes.size(); i++) {
            Atom bindingAtom = bindings.get(i);
            String bindingName = bindingAtom.value();

            // deal with &rest variadic args if present
            if (bindingName.equals("&rest")) {
                // 1. get next binding - this is the name of the list
                String restBindingName = bindings.get(i + 1).value();
                // 2. add this list binding to the boundArgs; we're not copying them as they were provided for this macro application
                boundArgs.put(restBindingName, entireList.fromIndex(i + 1));
                // 3. break out of loop; no more args
                break;
            }

            Node operandNode = operandNodes.get(i);
            boundArgs.put(bindingName, operandNode);
        }
        return boundArgs;
    }

    private NodeBuilder expand(Atom templateAtom,
                               Map<String, Node> bindingsMap,
                               boolean quasiquote) {
        if (bindingsMap.containsKey(templateAtom.value())) {
            Node replacement = bindingsMap.get(templateAtom.value());
            if (replacement instanceof Atom) {
                Atom.Builder transformedAtomBuilder = new Atom.Builder();
                transformedAtomBuilder.value(((Atom) replacement).value());
                return transformedAtomBuilder;
            } else {
                RList templateList = (RList) replacement;
                return expand(templateList, bindingsMap, quasiquote);
            }
        } else {
            Atom.Builder transformedAtomBuilder = new Atom.Builder();
            transformedAtomBuilder.forAtom(templateAtom);
            return transformedAtomBuilder;
        }
    }

    private NodeBuilder expand(RList templateList,
                               Map<String, Node> bindingsMap,
                               boolean quasiquote) {

        // Give particular attention to the first node - we need to handle quoting carefully.
        Node firstNode = templateList.get(0);

        // Deal with unquote - expect 2 nodes in total.
        if (firstNode instanceof Atom possibleUnquote) {
            if (possibleUnquote.value().equals("unquote")) {
                Node secondNode = templateList.get(1);
                if (secondNode instanceof Atom unquotedAtom) {
                    // we expand by definition because we're unquoting ... if we were quoting we wouldn't expand it
                    return expand(unquotedAtom, bindingsMap, quasiquote);
                } else if (secondNode instanceof RList unquotedRList) {
                    return expand(unquotedRList, bindingsMap, quasiquote);
                }
                else {
                    throw new IllegalStateException("unhandled Node type");
                }
            }
        }

        // Deal with quasiquote - expect 2 nodes in total
        if (firstNode instanceof Atom possibleQuote) {
            String value = possibleQuote.value();
            // todo not handling 'list' or 'quote' around the body, but adding them breaks any inner lists/quotes
            if (value.equals("quasiquote")) {
                Node secondNode = templateList.get(1);
                if (secondNode instanceof Atom quasiquotedAtom) {
                    return expand(quasiquotedAtom, bindingsMap, true);
                } else if (secondNode instanceof RList quasiquotedRList) {
                    return expand(quasiquotedRList, bindingsMap, true);
                }
                else {
                    throw new IllegalStateException("unhandled Node type");
                }
            }
        }

        RList.Builder expandedListBuilder = new RList.Builder();
        for (Node node : templateList.nodes()) {
            // This if/else seems a bit pointless - we do the same thing in each branch, but
            // we just need to dispatch to the appropriate overloaded method for the Node subtype
            if (node instanceof Atom templateAtom) {
                NodeBuilder expanded = expand(templateAtom, bindingsMap, quasiquote);
                expandedListBuilder.addNodeBuilder(expanded);
            } else {
                NodeBuilder transformedRListBuilder = expand((RList) node, bindingsMap, quasiquote);
                expandedListBuilder.addNodeBuilder(transformedRListBuilder);
            }
        }

        return expandedListBuilder;
    }
}
