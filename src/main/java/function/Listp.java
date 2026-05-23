package function;

import evaluator.env.Environment;
import value.ConsCellValue;
import value.Value;

import java.util.List;

public class Listp implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.size() != 1) {
            throw new IllegalArgumentException("Expect 1 arg for listp");
        }

        if(operands.getFirst() instanceof ConsCellValue) {
            return Value.t();
        }
        return Value.nil();
    }
}
