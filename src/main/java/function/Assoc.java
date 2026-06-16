package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.*;

import java.util.List;

public class Assoc implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // arg 1 is the symbol value for the item being looked up
        Value<?> argValue = operands.getFirst();

        // todo hack in a StringValue read from console into a SymbolValue :-/
//        if(symbolArg instanceof StringValue stringArg) {
//            String value = stringArg.getValue();
//            Symbol fakedSymbol = Symbols.internSymbol(value);
//            symbolArg = new SymbolValue(fakedSymbol);
//        }

//        if(symbolArg instanceof SymbolValue symbolValue) {
//            Symbol symbol = symbolValue.getValue();

            // todo nasty nesting here
            Value<?> consArg = operands.get(1);
            if(consArg instanceof ConsCellValue consCellValue) {
                ConsCell consCell = consCellValue.getValue();
                for(Value<?> pairValue : consCell) {
                    if(pairValue instanceof ConsCellValue pairConsCellValue) {
                        Value<?> carValue = pairConsCellValue.getValue().car();
                        if(carValue.equals(argValue)) {
                            return pairValue;
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
//        }
//        else {
//            throw new EvaluationException("assoc: expect quoted symbol for first arg");
//        }
        return Value.nil();
    }
}
