package function;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import value.Symbol;
import value.SymbolValue;
import value.Value;

import java.util.List;

public class And implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        for(Value<?> operand : operands) {
            if(!(operand instanceof SymbolValue)) {
                throw new IllegalArgumentException("and expects symbol operands only");
            }
            Symbol symbol = ((SymbolValue)operand).getValue();
            if(symbol == Symbols.t()) {
                continue;
            }
            else if(symbol == Symbols.nil()) {
                return new SymbolValue(Symbols.nil());
            }
            else {
                throw new IllegalArgumentException("and expects symbol operands t or nil only");

            }
        }
        return new SymbolValue(Symbols.t());
    }
}
