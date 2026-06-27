package function;

import evaluator.env.Environment;
import value.IntegerValue;
import value.Value;
import evaluator.env.Symbols;
import value.Symbol;
import value.SymbolValue;
import value.IntegerValue;

import java.util.List;
import java.util.Optional;

public class Gensym implements Function {

    @Override
    public Value<Symbol> apply(List<Value<?>> operands, Environment environment) {
        // TODO optional integer arg

        Symbol gensymCounterSymbol = Symbols.internSymbol("*gensym-counter*");
        Value<?> gensymCounterVal = environment.getVariable(gensymCounterSymbol)
            .orElseThrow(() -> new RuntimeException("Can't find *gensym-counter* in environment"));

        if (gensymCounterVal instanceof IntegerValue gensymIntValue) {
            int gensymCounter = gensymIntValue.getValue();

            environment.setBuiltInVariable(gensymCounterSymbol, new IntegerValue(gensymCounter+1));

            String newGensym = "#:G" + gensymCounter;
            // TODO don't intern
            return new SymbolValue(Symbols.internSymbol(newGensym));
        }

        throw new RuntimeException("*gensym-counter* value is not numeric");
    }
}
