package function;

import evaluator.env.Environment;
import value.ConsCell;
import value.ConsCellValue;
import value.Value;

import java.util.List;

public class Cons implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> car = operands.get(0);
        Value<?> cdr = operands.get(1);

        // only 2 possibilities:
        // 1. cdr is nil and car is non-nil: we are constructing the initial (tail) cons cell
        // 2. cdr is a ConsCell and car is non-nil: we are constructing an additional cons cell

        ConsCell consCell;
        if(Value.nil().equals(cdr) && !Value.nil().equals(car)) {
            // first cons cell
            consCell = new ConsCell(car, cdr);
            return new ConsCellValue(consCell);
        }
        else if(!Value.nil().equals(cdr) && !Value.nil().equals(car)) {
            // subsequent cons cell
            consCell = new ConsCell(car, cdr);
            return new ConsCellValue(consCell);
        }
        return null;
    }
}
