package function;

import evaluator.env.Environment;
import value.*;

import java.util.Iterator;
import java.util.List;

/**
 * Arg 1: function object
 * Arg 2: list
 */
public class Mapcar implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operatorValue = operands.getFirst();
        Value<?> listValue = operands.get(1);

        if (listValue instanceof ConsCellValue consCellValue) {
            ConsCell inputCons = consCellValue.getValue();
            Iterator<Value<?>> consIterator = inputCons.iterator();
            ConsCell outputCons = ConsCell.empty();
            if(operatorValue instanceof FunctionValue functionValue) {
                Function function = functionValue.getValue();

                while (consIterator.hasNext()) {
                    Value<?> currentInputValue = consIterator.next();
                    Value<?> currentOutputValue = function.apply(List.of(currentInputValue), environment);
                    outputCons = outputCons.push(currentOutputValue);
                }

                // Reverse because we walked a cons cell structure in the reverse of its insertion order
                outputCons = ConsCell.reverse(outputCons);

                return new ConsCellValue(outputCons);
            }
            else if (operatorValue instanceof ClosureValue closureValue) {
                throw new UnsupportedOperationException("unimplemented");
            }
            throw new UnsupportedOperationException("unhandled function object type " + operatorValue);
        }
        else {
            throw new UnsupportedOperationException("mapcar: list not provided: " + listValue);
        }
    }
}
