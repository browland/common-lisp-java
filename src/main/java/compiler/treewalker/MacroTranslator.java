package compiler.treewalker;

import evaluator.Evaluator;
import evaluator.Interpreter;
import evaluator.ScriptLoader;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import evaluator.macro.MacroExpander;
import function.FunctionDefinitions;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Macro;
import value.MacroValue;
import value.Symbol;
import value.Value;

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
    private final Map<String,Macro> localMacros = new HashMap<>();

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
        // First pass: look for any top-level defmacro forms and store them.
        for (Node node : nodes) {
            if (node instanceof RList rlist) {
                Node firstNode = rlist.nodes().getFirst();
                if (firstNode instanceof Atom atom) {
                    if ("defmacro".equals(atom.value())) {
                        Macro macro = createMacro(rlist);
                        localMacros.put(macro.getName(), macro);
                    }
                }
            }
        }

        // TODO need to ensure we eval top-level forms so we get defined functions which are needed by macro expansion.
        //      But need to avoid regular evaluation behaviour which will treat macro invocations as "expand and then
        //      eval resulting code" and instead just replace them in-place with the expanded code.
        //      Can cheat by just eval'ing each form naively and then reprocess it via macro translator?

        for (Node node : nodes) {
            evaluator.evaluate(node, environment);
        }

        // Second pass: Do recursive traversal of Nodes and look for invocations of any defined macros.
        // We still need to evaluate any forms we encounter as macro definitions might use them.
        List<Node> transformedNodes = new ArrayList<>();
        for (Node node : nodes) {
            transformedNodes.add(expandNodeIfMacroCall(localMacros, node));
        }

        System.out.println("Transformed forms: " + transformedNodes);

        return transformedNodes;
    }

    private Node expandNodeIfMacroCall(Map<String,Macro> macros, Node node) {
        if (node instanceof RList rlist) {
            if (rlist.nodes().isEmpty()) {
                return node;
            }
            Node operatorNode = rlist.nodes().getFirst();
            if (operatorNode instanceof Atom(String value)) {
                Macro matchedMacro = null;

                if (macros.containsKey(value)) {
                    matchedMacro = macros.get(value);
                }
                else if (environment.getMacro(Symbols.internSymbol(value)).isPresent()) {
                    Symbol macroSymbol = Symbols.internSymbol(value);
                    Optional<Value<?>> optionalMacroValue = environment.getMacro(macroSymbol);
                    if (optionalMacroValue.isPresent()) {
                        Value<?> possibleMacroValue = optionalMacroValue.get();
                        if (possibleMacroValue instanceof MacroValue macroValue)
                            matchedMacro = macroValue.getValue();
                    }
                }

                if (matchedMacro != null) {
                    Node expandedNode = macroExpander.expand(matchedMacro, rlist, evaluator, environment);
                    // Always look for a macro call one more time when if we found a macro to deal with inner macro calls
                    return expandNodeIfMacroCall(macros, expandedNode);
                }
            }

            // Look for macro calls somewhere within this form
            RList transformedList = new RList();
            for (Node innerNode : rlist.nodes()) {
                Node transformedNode = expandNodeIfMacroCall(macros, innerNode);
                // Always look for a macro call one more time when if we found a macro to deal with inner macro calls
                transformedNode = expandNodeIfMacroCall(macros, transformedNode);
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
        return localMacros.keySet();
    }
}
