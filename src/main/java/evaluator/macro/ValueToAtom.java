package evaluator.macro;

import syntaxtree.Atom;
import value.IntegerValue;
import value.StringValue;
import value.Symbol;
import value.SymbolValue;

public class ValueToAtom {
    static Atom toAtom(StringValue stringValue) {
        return new Atom("\"" + stringValue.getValue() + "\"", null);
    }

    static Atom toAtom(IntegerValue integerValue) {
        return new Atom(Integer.toString(integerValue.getValue()), null);
    }

    static Atom toAtom(SymbolValue symbolValue) {
        Symbol symbol = symbolValue.getValue();
        return new Atom(symbol.name(), null);
    }
}
