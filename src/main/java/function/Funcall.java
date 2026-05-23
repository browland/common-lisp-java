package function;

import evaluator.env.Environment;
import value.ClosureValue;
import value.FunctionValue;
import value.Value;

import java.util.List;

public class Funcall implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operatorValue = operands.getFirst();
        List<Value<?>> operandsWithoutOperator = operands.subList(1, operands.size());

        if(operatorValue instanceof FunctionValue functionValue) {
            Function function = functionValue.getValue();
            return function.apply(operandsWithoutOperator, environment);
        }
        else if (operatorValue instanceof ClosureValue closureValue) {
            Closure closure = closureValue.getValue();
            return closure.apply(operandsWithoutOperator, environment);
        }
        throw new UnsupportedOperationException("unhandled function object type " + operatorValue);
    }
}
