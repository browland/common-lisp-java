package evaluator.macro;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Namespace;
import exception.EvaluationException;
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
                       Evaluator evaluator,
                       Environment environment) {
        List<Node> bindings = macro.getBindings();
        List<Node> bodyNodes = macro.getBodyNodes();

        // convert bindings to values (not looking up from environment)
        List<Node> operandNodes = entireList.nodes().subList(1, entireList.size());
        Map<Symbol, Value<?>> bindingsMap;
        try {
            bindingsMap = bindingEvaluator.assignBindingsFromNodeOperands(bindings, operandNodes);
        }
        catch(EvaluationException e) {
            throw new EvaluationException(String.format("While expanding %s: %s", macro, e));
        }

        try {
            environment.enterScope();
            for (Symbol bindingSymbol : bindingsMap.keySet()) {
                environment.declareLexical(bindingSymbol, bindingsMap.get(bindingSymbol), Namespace.VARIABLE);
            }

            Value<?> expandedValue = null;
            for(Node bodyNode : bodyNodes) {
                expandedValue = evaluator.evaluate(bodyNode, environment);
            }

            return switch(expandedValue) {
                case ConsCellValue consCellValue -> consToRList.translate(consCellValue);
                case StringValue stringValue -> ValueToAtom.toAtom(stringValue);
                case SymbolValue symbolValue -> ValueToAtom.toAtom(symbolValue);
                case IntegerValue integerValue -> ValueToAtom.toAtom(integerValue);
                case ClosureValue closureValue -> ClosureToLambda.toLambdaNode(closureValue);
                case null -> throw new IllegalArgumentException("null returned during macro expansion");
                default -> throw new IllegalArgumentException("not ready yet for other value types");
            };

        } finally {
            environment.leaveScope();
        }
    }
}
