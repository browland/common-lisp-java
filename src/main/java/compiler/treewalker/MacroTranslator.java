package compiler.treewalker;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.macro.MacroExpander;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Macro;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Separate phase where the program is inspected for any macro definitions - these are stored and then any invocations
 * of these macros are expanded.  The overall result is a new Node tree, yet with macros expanded in a separate compile-
 * time execution context.
 */
public class MacroTranslator {
    private final MacroExpander macroExpander = new MacroExpander();
    private final Evaluator evaluator = new Evaluator();
    private final Environment environment = new Environment();

    public List<Node> transformMacros(List<Node> nodes) {
        // First pass: look for any top-level defmacro forms and store them.  This has the side-effect of allowing macros
        // to be invoked before declaration, but for now no big deal.

        Map<String,Macro> macros = new HashMap<>();
        for (Node node : nodes) {
            if (node instanceof RList rlist) {
                Node firstNode = rlist.nodes().getFirst();
                if (firstNode instanceof Atom atom) {
                    if ("defmacro".equals(atom.value())) {
                        Macro macro = createMacro(rlist);
                        macros.put(macro.getName(), macro);
                    }
                }
            }
        }

        // Second pass: Do recursive traversal of Nodes and look for invocations of any defined macros.
        List<Node> transformedNodes = new ArrayList<>();
        for (Node node : nodes) {
            transformedNodes.add(expandNodeIfMacro(macros, node));
        }
        return transformedNodes;
    }

    private Node expandNodeIfMacro(Map<String,Macro> macros, Node node) {
        if (node instanceof RList rlist) {
            if (rlist.nodes().isEmpty()) {
                return node;
            }
            Node operatorNode = rlist.nodes().getFirst();
            if (operatorNode instanceof Atom operatorAtom) {
                if (macros.containsKey(operatorAtom.value())) {
                    Macro macro = macros.get(operatorAtom.value());
                    return macroExpander.expand(macro, rlist, evaluator, environment);
                }
            }
            RList transformedList = new RList();
            for (Node innerNode : rlist.nodes()) {
                Node transformedNode = expandNodeIfMacro(macros, innerNode);
                transformedList.add(transformedNode);
            }
            return transformedList;
        }
        else {
            return node;  // atom doesn't need any transformation.
        }
    }

    private Macro createMacro(RList macroForm) {
        RList bindingsList = (RList)macroForm.get(2);
        List<Node> bindings = bindingsList.nodes();

        List<Node> allNodes = macroForm.nodes();
        List<Node> bodyNodes = allNodes.subList(3, allNodes.size());

        Atom nameAtom = (Atom)macroForm.get(1);
        String name = nameAtom.value();

        return new Macro(bindings, bodyNodes, name);
    }
}
