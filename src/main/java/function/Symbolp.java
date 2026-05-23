package function;

import evaluator.env.Environment;
import value.SymbolValue;
import value.Value;

import java.util.List;

public class Symbolp implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.size() != 1) {
            throw new IllegalArgumentException("symbolp expects one argument");
        }

        if(operands.get(0) instanceof SymbolValue) {
            return Value.t();
        }
        else {
            return Value.nil();
        }
    }
}
