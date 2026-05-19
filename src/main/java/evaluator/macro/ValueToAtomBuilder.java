package evaluator.macro;

import syntaxtree.Atom;
import value.IntegerValue;
import value.StringValue;
import value.Symbol;
import value.SymbolValue;

public class ValueToAtomBuilder {
    static Atom.Builder atomBuilder(StringValue stringValue) {
        return new Atom.Builder()
                .value("\"" + stringValue.getValue() + "\"");
    }

    static Atom.Builder atomBuilder(IntegerValue integerValue) {
        return new Atom.Builder()
                .value(Integer.toString(integerValue.getValue()));
    }

    static Atom.Builder atomBuilder(SymbolValue symbolValue) {
        Symbol symbol = symbolValue.getValue();
        return new Atom.Builder()
                .value(symbol.name());
    }
}
