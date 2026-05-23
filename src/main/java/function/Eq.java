package function;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import value.*;

import java.util.List;

/**
 * Works like Java's == operator (tests for reference equality, so makes most sense to compare
 * strings, symbols, integers etc.
 */
public class Eq implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        Value<?> operand1 = operands.get(0);
        Value<?> operand2 = operands.get(1);

        if(operand1 instanceof SymbolValue && operand2 instanceof SymbolValue) {
            Symbol sym1 = ((SymbolValue)operand1).getValue();
            Symbol sym2 = ((SymbolValue)operand2).getValue();

            return sym1.equals(sym2) ? new SymbolValue(Symbols.t())
                    : new SymbolValue(Symbols.nil());
        }
        else if(operand1 instanceof IntegerValue && operand2 instanceof IntegerValue) {
            int int1 = ((IntegerValue)operand1).getValue();
            int int2 = ((IntegerValue)operand2).getValue();

            return int1 == int2 ? new SymbolValue(Symbols.t())
                    : new SymbolValue(Symbols.nil());
        }
        throw new IllegalArgumentException("eq not fully implemented");
    }
}
