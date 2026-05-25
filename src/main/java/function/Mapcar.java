package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.*;

import java.util.ArrayList;
import java.util.List;

public class Mapcar implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> functionOperand = operands.getFirst();
        Function function;
        if (functionOperand instanceof FunctionValue functionValue) {
            function = functionValue.getValue();
        } else if (functionOperand instanceof ClosureValue closureValue) {
            function = closureValue.getValue();
        } else {
            throw new EvaluationException("mapcar: require function as first argument");
        }

        List<Value<?>> resultTemp = new ArrayList<>();
        Value<?> listValue = operands.get(1);
        if(listValue instanceof ConsCellValue listConsCellValue) {
            ConsCell listCons = listConsCellValue.getValue();
            for(Value<?> car : listCons) {
                Value<?> result = function.apply(List.of(car), environment);
                resultTemp.add(result);
            }
        }

        resultTemp = resultTemp.reversed();
        ConsCell cons = null;
        for (Value<?> toPush : resultTemp) {
            if (cons == null) {
                cons = ConsCell.fromValue(toPush);
            } else {
                cons = cons.push(toPush);
            }
        }
        return new ConsCellValue(cons);
    }
}
