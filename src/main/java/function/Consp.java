package function;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import value.ConsCellValue;
import value.SymbolValue;
import value.Value;

import java.util.List;

public class Consp implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operand = operands.get(0);
        if(operand instanceof ConsCellValue) {
            return new SymbolValue(Symbols.t());
        }
        else {
            return new SymbolValue(Symbols.nil());
        }
    }
}
