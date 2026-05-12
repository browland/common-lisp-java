package function;

import evaluator.BindingEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Node;
import value.Symbol;
import value.Value;

import java.util.List;
import java.util.Map;

public record Closure(Evaluator evaluator,
                      BindingEvaluator bindingEvaluator,
                      Environment capturedEnvironment,
                      List<Node> bindings,
                      Node body) implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment noApplyTimeEnv) {
        // The application environment passed in at application time cannot be used.  We can only access the captured
        // environment at creation (evaluation) time, and the values of the bindings passed at application time.  Not
        // any of the lexical scope at apply time.  So we ignore noApplicationEnvironment.

        // Order matters - ensure bound arguments shadow (overwrite) variables with the same name from the environment
        // captured at closure creation time

        Map<Symbol, Value<?>> bindingsMap = bindingEvaluator.assignBindingsFromValueOperands(bindings, operands);

        capturedEnvironment.enterScope();
        for(Symbol bindingSymbol : bindingsMap.keySet()) {
            capturedEnvironment.setInScope(bindingSymbol, bindingsMap.get(bindingSymbol));
        }

        Value<?> evalResult = evaluator.evaluate(body, capturedEnvironment);
        capturedEnvironment.leaveScope();
        return evalResult;
    }
}
