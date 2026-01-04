package evaluator;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import value.*;

import java.util.Optional;

public class AtomEvaluator {
    Value<?> atomToValue(Atom atom, Environment environment) {
        String atomStringValue = atom.value();
        if(atomStringValue.startsWith(":")) {
            // keyword symbol - a literal symbol which evaluates to itself
            Symbol symbol = environment.internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
        else if(atomStringValue.startsWith("\"") && atomStringValue.endsWith("\"")) {
            String stringWithoutQuotes = atomStringValue.substring(1, atomStringValue.length()-1);
            return new StringValue(stringWithoutQuotes);
        }
        else {
            // could be in the environment; otherwise fall back to int
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            Optional<Value<?>> possibleValue = environment.get(symbol);
            if(possibleValue.isPresent()) {
                return possibleValue.get();
            }

            int intValue = Integer.parseInt(atomStringValue);
            return new IntegerValue(intValue);
        }
    }
}
