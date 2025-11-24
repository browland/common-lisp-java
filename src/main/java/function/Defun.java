package function;

import evaluator.Evaluator;
import value.Value;
import syntaxtree.RList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Defun(Evaluator evaluator,
                    String name,
                    List<String> bindings,
                    RList body) implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Map<String,Value<?>> noApplicationEnvironment) {
        Map<String,Value<?>> bindingsMap = new HashMap<>();
        for(int i=0; i<operands.size(); i++) {
            bindingsMap.put(bindings.get(i), operands.get(i));
        }

        return evaluator.evaluate(body, bindingsMap);
    }
}
