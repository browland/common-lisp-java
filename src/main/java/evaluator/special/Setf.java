package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.Symbol;
import value.Value;
import value.ValueType;

import java.util.Optional;

public class Setf implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        Atom symbolAtom = (Atom) entireList.get(1);
        if((symbolAtom.prefix() != null && !symbolAtom.prefix().isEmpty())
                || symbolAtom.suffix() != null) {
            throw new IllegalArgumentException("name for defvar must be a symbol: [" + symbolAtom + "]");
        }

        String name = symbolAtom.value();
        Symbol symbol = environment.getSymbols().internSymbol(name);

        // for now we only implement setf for lists.  The list must already exist at the given symbol.
        if(environment.get(symbol).isEmpty()) {
            throw new UnsupportedOperationException("Cannot setf a list which isn't bound: " + name);
        }

        Optional<Value<?>> optionalBoundConsOrNil = environment.get(symbol);
        if(optionalBoundConsOrNil.isEmpty()) {
            throw new IllegalArgumentException("could not find symbol for setf: " + name);
        }

        Value<?> boundConsOrNil = optionalBoundConsOrNil.get();
        if(!(boundConsOrNil.getType() == ValueType.CONS_CELL || boundConsOrNil.equals(Value.nil()))) {
            throw new IllegalArgumentException("can only setf into a cons cell for now: " + name);
        }

        // evaluate the value being set to the symbol
        Value<?> value = evaluator.evaluate(entireList.nodes().get(2), environment);

        // ensure it's a list for now
        if(value.getType() != ValueType.CONS_CELL) {
            throw new IllegalArgumentException("can only setf a cons cell for now: " + value);
        }

        // todo bug: assuming global only!
        environment.setGlobal(symbol, value);
        return value;
    }
}
