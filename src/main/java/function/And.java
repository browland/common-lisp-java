package function;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import value.SymbolValue;
import value.Value;

import java.util.List;

public class And implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        for(Value<?> operand : operands) {
            if(operand instanceof SymbolValue symbolValue) {
                if(symbolValue.getValue().equals(Symbols.nil())) {
                    return Value.nil();
                }

            }
        }
        return Value.t();
    }
}
