package evaluator.macro;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Node;
import syntaxtree.RList;
import value.*;

import java.util.List;
import java.util.Map;

public class MacroExpander {
    private final BindingEvaluator bindingEvaluator = new BindingEvaluator();
    private final ConsToRList consToRList = new ConsToRList();

    public Node expand(Macro macro,
                       RList entireList,
                       Evaluator evaluator) {
        Environment localEnv = new Environment();  // Todo I think this should be a ScopeEnvironment, otherwise redefining globals etc
        List<Node> bindings = macro.getBindings();
        RList bodyTemplate = macro.getBody();

        // convert bindings to values (not looking up from environment)
        List<Node> operandNodes = entireList.nodes().subList(1, entireList.size());
        Map<Symbol, Value<?>> bindingsMap = bindingEvaluator.assignBindingsFromNodeOperands(bindings, operandNodes);

        try {
            localEnv.enterScope();
            for (Symbol bindingSymbol : bindingsMap.keySet()) {
                localEnv.setInScope(bindingSymbol, bindingsMap.get(bindingSymbol));
            }

            Value<?> expandedValue = evaluator.evaluate(bodyTemplate, localEnv);
            if (expandedValue instanceof ConsCellValue consCellValue) {
                return consToRList.translate(consCellValue);
            } else if (expandedValue instanceof StringValue stringValue) {
                return ValueToAtomBuilder.atomBuilder(stringValue).build();
            } else if (expandedValue instanceof SymbolValue symbolValue) {
                return ValueToAtomBuilder.atomBuilder(symbolValue).build();
            }
            throw new IllegalArgumentException("not ready yet for other value types");
        } finally {
            localEnv.leaveScope();
        }
    }
}
