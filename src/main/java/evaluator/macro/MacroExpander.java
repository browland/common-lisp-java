package evaluator.macro;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
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
                        Evaluator evaluator,
                        Environment env) {
        // todo should not use parent env - use a brand new one just for macro expansion with the bindings only
        List<Atom> bindings = macro.getBindings();
        RList bodyTemplate = macro.getBody();

        // evaluate bindings
        List<Node> operandNodes = entireList.nodes().subList(1, entireList.size());
        Map<Symbol, Value<?>> bindingsMap = bindingEvaluator.evaluateWithNodes(bindings, operandNodes, env);

        try {
            env.enterScope();
            for (Symbol bindingSymbol : bindingsMap.keySet()) {
                env.setInScope(bindingSymbol, bindingsMap.get(bindingSymbol));
            }

            Value<?> evaluatedList = evaluator.evaluate(bodyTemplate, env);
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
            env.leaveScope();
        }
    }
}
