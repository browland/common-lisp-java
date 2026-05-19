package evaluator;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import value.*;

import java.util.Optional;

public class AtomEvaluator {
    Value<?> atomToValueWithLookup(Atom atom,
                                   Environment environment) {
        String atomStringValue = atom.value();
        if(atomStringValue.startsWith(":")) {
            // keyword symbol - a literal symbol which evaluates to itself
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
        else if(atomStringValue.startsWith("\"") && atomStringValue.endsWith("\"")) {
            String stringWithoutQuotes = atomStringValue.substring(1, atomStringValue.length()-1);
            return new StringValue(stringWithoutQuotes);
        }
        else {
            // could be in the environment; otherwise fall back to int
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            Optional<Value<?>> possibleValue = environment.getVariable(symbol);
            if(possibleValue.isPresent()) {
                return possibleValue.get();
            }

            int intValue = Integer.parseInt(atomStringValue);
            return new IntegerValue(intValue);
        }
    }

    Value<?> atomToValueNoLookup(String atomStringValue) {
        if(atomStringValue.startsWith(":")) {
            // keyword symbol - a literal symbol which evaluates to itself
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
        else if(atomStringValue.startsWith("\"") && atomStringValue.endsWith("\"")) {
            String stringWithoutQuotes = atomStringValue.substring(1, atomStringValue.length()-1);
            return new StringValue(stringWithoutQuotes);
        }
        else if(isNumeric(atomStringValue)) {
            int intValue = Integer.parseInt(atomStringValue);
            return new IntegerValue(intValue);
        }
        else {
            // treat as symbol
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
