package evaluator.macro;

import evaluator.Evaluator;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.NodeBuilder;
import syntaxtree.RList;
import value.Macro;
import value.Value;
import value.ValueType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MacroEvaluator {
    public Optional<Value<?>> evaluate(String operatorName,
                                       RList entireList,
                                       Map<String, Value<?>> environment,
                                       Evaluator evaluator) {

        Value<?> macroValue = environment.get(operatorName);
        if(macroValue == null) {
            return Optional.empty();
        }
        if(macroValue.type() != ValueType.MACRO) {
            return Optional.empty();
        }

        Macro macro = (Macro)macroValue.value();
        List<Atom> bindings = macro.getBindings();
        RList bodyTemplate = macro.getBody();

        Map<String, Node> bindingsMap = prepareBindings(entireList, bindings);

        RList.Builder transformedBodyBuilder = transform(bodyTemplate, bindingsMap, true, null);
        RList transformedBody = transformedBodyBuilder.build();

        Map<String, Value<?>> capturedEnvironment = macro.getCapturedEnvironment();
        Map<String,Value<?>> capturedEnvironmentPlusBindings = new HashMap<>(capturedEnvironment);
        Value<?> evaluatedMacro = evaluator.evaluate(transformedBody, capturedEnvironmentPlusBindings);
        return Optional.of(evaluatedMacro);
    }

    private Map<String, Node> prepareBindings(RList entireList, List<Atom> bindings) {
        Map<String,Node> boundArgs = new HashMap<>();
        List<Node> operandNodes = entireList.nodes().subList(1, entireList.size());
        for(int i = 0; i< operandNodes.size(); i++) {
            Atom bindingAtom = bindings.get(i);
            String bindingName = bindingAtom.value();

            // deal with &rest variadic args if present
            if(bindingAtom.prefix() != null && bindingAtom.prefix().contains("&") && bindingAtom.value().equals("rest")) {
                // 1. get next binding - this is the name of the list
                String restBindingName = bindings.get(i+1).value();
                // 2. add this list binding to the boundArgs; we're not copying them as they were provided for this macro application
                boundArgs.put(restBindingName, entireList.fromIndex(i+1));
                // 3. break out of loop; no more args
                break;
            }

            Node operandNode = operandNodes.get(i);
            boundArgs.put(bindingName, operandNode);
        }
        return boundArgs;
    }

    private NodeBuilder transform(Atom templateAtom,
                                  Map<String, Node> bindingsMap) {
        if(templateAtom.prefix() != null && templateAtom.prefix().contains(",") && bindingsMap.containsKey(templateAtom.value())) {
            // We can now treat the comma (unquote) prefix as consumed and should not be considered again
            String remainingPrefix = templateAtom.prefix().replace(",", "");
            Node replacement = bindingsMap.get(templateAtom.value());
            if(replacement instanceof Atom) {
                Atom.Builder transformedAtomBuilder = new Atom.Builder();
                transformedAtomBuilder.value(((Atom) replacement).value());
                transformedAtomBuilder.prefix(remainingPrefix);
                return transformedAtomBuilder;
            }
            else {
                RList templateList = (RList)replacement;
                return transform(templateList, bindingsMap, false, remainingPrefix);
            }
        }
        else {
            Atom.Builder transformedAtomBuilder = new Atom.Builder();
            transformedAtomBuilder.forAtom(templateAtom);
            return transformedAtomBuilder;
        }
    }

    private RList.Builder transform(RList templateList,
                                    Map<String, Node> bindingsMap,
                                    boolean unquote,
                                    String transferredPrefix) {
        RList.Builder generatedListBuilder = new RList.Builder();
        generatedListBuilder.prefix(transferredPrefix);

        if(unquote) {
            // we only discard the quasi-quote as we're just always unquoting anything anyway
            generatedListBuilder.prefix(templateList.prefix().replace("`", ""));

        }
        else {
            String prefix = generatedListBuilder.getPrefix();
            if(prefix != null) {
                generatedListBuilder.prefix(prefix+templateList.prefix());
            }
            else {
                generatedListBuilder.prefix(templateList.prefix());
            }
        }

        for(Node node : templateList.nodes()) {
            if(node instanceof Atom) {
                Atom templateAtom = (Atom)node;
                generatedListBuilder.addNodeBuilder(transform(templateAtom, bindingsMap));
            }
            else {
                RList.Builder transformedRListBuilder = transform((RList) node, bindingsMap, false, null);
                generatedListBuilder.addNodeBuilder(transformedRListBuilder);
            }
        }

        return generatedListBuilder;
    }
}
