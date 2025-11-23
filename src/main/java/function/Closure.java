package function;

import evaluator.Evaluator;
import evaluator.Value;
import syntaxtree.RList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Closure(Evaluator evaluator,
                      Map<String,String> capturedEnvironment,
                      List<String> bindings,
                      RList body) implements Function {

    @Override
    public Value<?> apply(List<String> operands, Map<String,String> noApplicationEnvironment) {
        // The application environment passed in at application time cannot be used.  We can only access the captured
        // environment at creation (evaluation) time, and the values of the bindings passed at application time.  Not
        // any of the lexical scope at apply time.  So we ignore noApplicationEnvironment.

        // Order matters - ensure bound arguments shadow (overwrite) variables with the same name from the environment
        // captured at closure creation time
        Map<String,String> capturedEnvironmentPlusBindings = new HashMap<>(capturedEnvironment);

        Map<String,String> bindingsMap = new HashMap<>();
        for(int i=0; i<operands.size(); i++) {
            bindingsMap.put(bindings.get(i), operands.get(i));
        }

        capturedEnvironmentPlusBindings.putAll(bindingsMap);

        return evaluator.evaluate(body, capturedEnvironmentPlusBindings);
    }
}
