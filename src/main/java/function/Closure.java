package function;

import evaluator.Evaluator;
import value.Value;
import syntaxtree.RList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Closure(Evaluator evaluator,
                      Map<String,Value<?>> capturedEnvironment,
                      List<String> bindings,
                      RList body,
                      String optionalName) implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Map<String,Value<?>> noApplicationEnvironment) {
        // The application environment passed in at application time cannot be used.  We can only access the captured
        // environment at creation (evaluation) time, and the values of the bindings passed at application time.  Not
        // any of the lexical scope at apply time.  So we ignore noApplicationEnvironment.

        // Order matters - ensure bound arguments shadow (overwrite) variables with the same name from the environment
        // captured at closure creation time
        Map<String,Value<?>> capturedEnvironmentPlusBindings = new HashMap<>(capturedEnvironment);

        Map<String,Value<?>> bindingsMap = new HashMap<>();
        for(int i=0; i<operands.size(); i++) {
            bindingsMap.put(bindings.get(i), operands.get(i));
        }

        capturedEnvironmentPlusBindings.putAll(bindingsMap);

        Value<?> evaluate = evaluator.evaluate(body, capturedEnvironmentPlusBindings);

        // todo need to deal with a linked list (stack) of environments, rather than doing this
        // todo 2 need to handle globals with prefix/suffix and properly
        for(String symbol : capturedEnvironmentPlusBindings.keySet()) {
           if(symbol.startsWith("*") && symbol.endsWith("*")) {
               capturedEnvironment.put(symbol, capturedEnvironmentPlusBindings.get(symbol));
           }
        }

        return evaluate;
    }

    public String toString() {
        return "closure";
    }
}
