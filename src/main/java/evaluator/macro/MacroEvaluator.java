package evaluator.macro;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.NodeBuilder;
import syntaxtree.RList;
import value.Macro;
import value.Symbol;
import value.Value;
import value.ValueType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MacroEvaluator {
    public Optional<Value<?>> evaluate(String operatorName,
                                       RList entireList,
                                       Environment environment,
                                       Evaluator evaluator) {

        Symbol symbol = environment.getSymbols().internSymbol(operatorName);
        Optional<Value<?>> optionalMacro = environment.get(symbol);
        if (optionalMacro.isEmpty()) {
            return Optional.empty();
        }

        Value<?> macroValue = optionalMacro.get();
        if (macroValue.getType() != ValueType.MACRO) {
            return Optional.empty();
        }

        Macro macro = (Macro) macroValue.getValue();
        List<Atom> bindings = macro.getBindings();
        RList bodyTemplate = macro.getBody();

        Map<String, Node> bindingsMap = prepareBindings(entireList, bindings);

        NodeBuilder transformedBodyBuilder = expand(bodyTemplate, bindingsMap, false);
        RList expandedBody = (RList) transformedBodyBuilder.build();

        Environment capturedEnvironment = macro.getCapturedEnvironment();
        Value<?> evaluatedMacro = evaluator.evaluate(expandedBody, capturedEnvironment);
        return Optional.of(evaluatedMacro);
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
        if (firstNode instanceof Atom possibleQuasiquote) {
            if (possibleQuasiquote.value().equals("quasiquote")) {
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

        // todo deal with (quasiquote x)
        //      x may be an atom or a list.
        //      Remove the quasiquote node, keep track that we're quasiquoting, and then essentially, go through each node
        //      in x, and either quote it or let unquote do its thing if present.
        //      Need to ensure recursive calls for unquote lists don't get re-quoted - e.g. pass a boolean through recursive
        //      calls to say we're quasiquoting, so quote anything which isn't explicitly being unquoted.
        //      Eventually should not quote any literals.  At the mo, we just have Atoms, we don't know if they're a
        //      symbol or integer or string or what.

        RList.Builder expandedListBuilder = new RList.Builder();
        for (Node node : templateList.nodes()) {
            if (node instanceof Atom) {
                Atom templateAtom = (Atom) node;
                // I ***think*** we can only get an atom back here ...
                Atom.Builder expandedAtom = (Atom.Builder)expand(templateAtom, bindingsMap, quasiquote);
                if(quasiquote) {
//                    RList.Builder quotedListBuilder = new RList.Builder();
//                    quotedListBuilder.addNodeBuilder(new Atom.Builder().value("quote"));
//                    quotedListBuilder.addNodeBuilder(expandedAtom);
//                    expandedListBuilder.addNodeBuilder(quotedListBuilder);
                    expandedListBuilder.addNodeBuilder(expandedAtom);
                }
                else {
                    expandedListBuilder.addNodeBuilder(expandedAtom);
                }
            } else {
                NodeBuilder transformedRListBuilder = expand((RList) node, bindingsMap, quasiquote);
                expandedListBuilder.addNodeBuilder(transformedRListBuilder);
            }
        }

        return expandedListBuilder;
    }
}
