package evaluator.special;

import evaluator.Evaluator;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.Map;

public class Setf implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Map<String, Value<?>> environment,
                             Evaluator evaluator) {
        Atom symbolAtom = (Atom) entireList.get(1);
        if((symbolAtom.prefix() != null && !symbolAtom.prefix().isEmpty())
                || symbolAtom.suffix() != null) {
            throw new IllegalArgumentException("name for defvar must be a symbol: [" + symbolAtom + "]");
        }

        String name = symbolAtom.value();

        // for now we only implement setf for lists.  The list must already exist at the given symbol.
        if(!environment.containsKey(name)) {
            throw new UnsupportedOperationException("Cannot setf a list which isn't bound: " + name);
        }

        Value<?> boundConsOrNil = environment.get(name);
        if(!(boundConsOrNil.type() == ValueType.CONS_CELL || boundConsOrNil.equals(Value.nil()))) {
            throw new IllegalArgumentException("can only setf into a cons cell for now: " + name);
        }

        // evaluate the value being set to the symbol
        Value<?> value = evaluator.evaluate(entireList.nodes().get(2), environment);

        // ensure it's a list for now
        if(value.type() != ValueType.CONS_CELL) {
            throw new IllegalArgumentException("can only setf a cons cell for now: " + value);
        }

        environment.put(name, value);
        return value;
    }
}
