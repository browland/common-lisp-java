package function;

import evaluator.env.Environment;
import value.*;

import java.util.List;

public class Cons implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> car = operands.get(0);
        Value<?> cdr = operands.get(1);

        // only 3 possibilities:
        // 1. cdr is nil and car is non-nil: we are constructing the initial (tail) cons cell
        // 2. cdr is a ConsCell and car is non-nil: we are constructing an additional cons cell
        // 3. cdr is an arbitrary value and car is non-nil: we are constructing a pair (improper list)

        // Actually starting to see more cases which can happen, e.g. (cons nil '()) which is fine.  So let's try to
        // be more lenient here.

        // Handle case when cdr is an empty list and we're consing a value onto it - should end up with just a
        // single cons cell with our new car and a cdr of nil.
        if(cdr instanceof ConsCellValue cdrConsValue) {
            ConsCell cdrCons = cdrConsValue.getValue();
            if((Value.nil().equals(cdrCons.car()) && Value.nil().equals(cdrCons.cdr()))
                    || (SymbolValue.nil().equals(cdrCons.car()) && SymbolValue.nil().equals(cdrCons.cdr()))) {
                return new ConsCellValue(new ConsCell(car, Value.nil()));
            }
        }

        ConsCell consCell = new ConsCell(car, cdr);
        return new ConsCellValue(consCell);
//        if(Value.nil().equals(cdr) && !Value.nil().equals(car)) {
//            // first cons cell
//            consCell = new ConsCell(car, cdr);
//            return new ConsCellValue(consCell);
//        }
//        else if(!Value.nil().equals(cdr) && !Value.nil().equals(car)) {
//            // subsequent cons cell
//            consCell = new ConsCell(car, cdr);
//            return new ConsCellValue(consCell);
//        }
//        return null;
    }
}
