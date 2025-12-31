package function;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import value.ConsCellValue;
import value.Symbol;
import value.Value;
import syntaxtree.RList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record Closure(Evaluator evaluator,
                      Environment capturedEnvironment,
                      List<Atom> bindings,
                      RList body) implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment noApplyTimeEnv) {
        // The application environment passed in at application time cannot be used.  We can only access the captured
        // environment at creation (evaluation) time, and the values of the bindings passed at application time.  Not
        // any of the lexical scope at apply time.  So we ignore noApplicationEnvironment.

        // Order matters - ensure bound arguments shadow (overwrite) variables with the same name from the environment
        // captured at closure creation time
        // todo generify handling of atom arg handling
        Map<String,Value<?>> bindingsMap = new HashMap<>();
        for(int i=0; i<operands.size(); i++) {
            Atom operandAtom = bindings.get(i);
            String bindingName = operandAtom.value();
            if(bindingName.equals("&rest")) {
                // 1. get next binding - this is the name of the list
                String restBindingName = bindings.get(i+1).value();
                // 2. get remaining operands - put them all in a list and assign to the name
                List<Value<?>> restValues = operands.subList(i, operands.size());
                ConsCellValue restValuesCons = ConsCellValue.fromJavaList(restValues);
                // 3. add this binding to the bindingsMap
                bindingsMap.put(restBindingName, restValuesCons);
                // 4. break out of loop
                break;
            }
            else {
                bindingsMap.put(bindingName, operands.get(i));
            }
        }

        capturedEnvironment.enterScope();
        for(String name : bindingsMap.keySet()) {
            Symbol symbol = capturedEnvironment.getSymbols().internSymbol(name);
            capturedEnvironment.setInScope(symbol, bindingsMap.get(name));
        }

        Value<?> evalResult = evaluator.evaluate(body, capturedEnvironment);
        capturedEnvironment.leaveScope();
        return evalResult;
    }
}
