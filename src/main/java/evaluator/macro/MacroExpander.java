package evaluator.macro;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Node;
import syntaxtree.RList;
import value.ConsCellValue;
import value.Macro;
import value.Symbol;
import value.Value;

import java.util.List;
import java.util.Map;

public class MacroExpander {
    private final BindingEvaluator bindingEvaluator = new BindingEvaluator();
    private final ConsToRList consToRList = new ConsToRList();

    public RList expand(Macro macro,
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

            Value<?> evaluatedList = evaluator.evaluate(bodyTemplate, localEnv);
            if (evaluatedList instanceof ConsCellValue consCellValue) {
                Node translatedNode = consToRList.translate(consCellValue);
                if(translatedNode instanceof RList translatedRList) {
                    return translatedRList;
                }
                else {
                    throw new IllegalStateException("macro expansion resulted in an Atom - not ready yet");
                }
            } else {
                throw new IllegalStateException("macro expansion did not result in a cons cell");
            }
        }
        finally {
            localEnv.leaveScope();
        }
    }
}
