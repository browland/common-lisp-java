package compiler.treewalker;

import evaluator.Evaluator;
import evaluator.Interpreter;
import evaluator.ScriptLoader;
import evaluator.env.Environment;
import evaluator.macro.MacroExpander;
import function.FunctionDefinitions;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Macro;

import java.io.IOException;
import java.util.*;

/**
 * Separate phase where the program is inspected for any macro definitions - these are stored and then any invocations
 * of these macros are expanded.  The overall result is a new Node tree, yet with macros expanded in a separate compile-
 * time execution context.
 */
public class MacroTranslator {
    private final MacroExpander macroExpander = new MacroExpander();
    private final Evaluator evaluator = new Evaluator();
    private final Environment environment = new Environment();
    private final Interpreter interpreter = new Interpreter(environment);
    private final ScriptLoader scriptLoader = new ScriptLoader(interpreter);
    private final Map<String,Macro> macros = new HashMap<>();

    public MacroTranslator(boolean loadScripts) {
        FunctionDefinitions.addFunctionDefinitions(environment);
        if (!loadScripts) {
            return;
        }
        try {
            scriptLoader.loadLibrary();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Node> transformMacros(List<Node> nodes) {
        // First pass: look for any top-level defmacro forms and store them.  This has the side-effect of allowing macros
        // to be invoked before declaration, but for now no big deal.

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
                    Node expandedNode = macroExpander.expand(macro, rlist, evaluator, environment);

                    // Always look for a macro call one more time when if we found a macro to deal with inner macro calls
                    return expandNodeIfMacro(macros, expandedNode);
                }
            }

            RList transformedList = new RList();
            for (Node innerNode : rlist.nodes()) {
                Node transformedNode = expandNodeIfMacro(macros, innerNode);
                // Always look for a macro call one more time when if we found a macro to deal with inner macro calls
                transformedNode = expandNodeIfMacro(macros, transformedNode);
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

    Set<String> getMacroNames() {
        return macros.keySet();
    }
}
