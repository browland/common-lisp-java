package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.ConsCell;
import value.ConsCellValue;
import value.Value;
import value.ValueType;

import java.util.Iterator;
import java.util.List;

public class Cadr implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // single operand - if it's not a ConsCell then error
        Value<?> operand = operands.getFirst();
        if(operand.getType() != ValueType.CONS_CELL) {
            throw new IllegalArgumentException("Can only invoke car on a list");
        }

        ConsCell consCell = (ConsCell) operand.getValue();
        Iterator<Value<?>> consCellIter = consCell.iterator();
        // Skip head element; we want the 2nd one
        if(consCellIter.hasNext()) {
            consCellIter.next();
        }
        else {
            return Value.nil();
        }

        if(consCellIter.hasNext()) {
            return consCellIter.next();
        }
        else {
            return Value.nil();
        }
    }
}
