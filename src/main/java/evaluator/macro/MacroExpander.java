package evaluator.macro;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
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
                environment.setInScope(bindingSymbol, bindingsMap.get(bindingSymbol));
            }

            Value<?> expandedValue = null;
            for(Node bodyNode : bodyNodes) {
                expandedValue = evaluator.evaluate(bodyNode, environment);
            }
            if (expandedValue instanceof ConsCellValue consCellValue) {
                return consToRList.translate(consCellValue);
            } else if (expandedValue instanceof StringValue stringValue) {
                return ValueToAtom.toAtom(stringValue);
            } else if (expandedValue instanceof SymbolValue symbolValue) {
                return ValueToAtom.toAtom(symbolValue);
            } else if (expandedValue instanceof IntegerValue integerValue) {
                return ValueToAtom.toAtom(integerValue);
            } else if(expandedValue instanceof ClosureValue closureValue) {
                return ClosureToLambda.toLambdaNode(closureValue);
            }

            // todo this happens when running MacroSpec."macro which expands to closure"()
            //      we eval the expanded macro and its value is not something we know how to convert back to a Node
            throw new IllegalArgumentException("not ready yet for other value types");
        } finally {
            environment.leaveScope();
        }
    }
}
