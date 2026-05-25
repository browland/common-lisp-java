package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.*;

import java.util.List;

public class Assoc implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // arg 1 is the symbol value for the item being looked up
        Value<?> symbolArg = operands.getFirst();
        if(symbolArg instanceof SymbolValue symbolValue) {
            Symbol symbol = symbolValue.getValue();

            // todo nasty nesting here
            Value<?> consArg = operands.get(1);
            if(consArg instanceof ConsCellValue consCellValue) {
                ConsCell consCell = consCellValue.getValue();
                for(Value<?> pairValue : consCell) {
                    if(pairValue instanceof ConsCellValue pairConsCellValue) {
                        Value<?> carValue = pairConsCellValue.getValue().car();
                        if(carValue instanceof SymbolValue thisSymbolValue) {
                            Symbol thisSymbol = thisSymbolValue.getValue();
                            if(thisSymbol.equals(symbol)) {
                                return pairValue;
                            }
                        }
                    }
                    else {
                        throw new EvaluationException("assoc: expect cons values in supplied cons");
                    }
                }
            }
            else {
                throw new EvaluationException("assoc: expect cons value for second arg");
            }
        }
        else {
            throw new EvaluationException("assoc: expect quoted symbol for first arg");
        }
        return Value.nil();
    }
}
