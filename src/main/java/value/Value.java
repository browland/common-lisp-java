package value;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;

import java.util.Objects;

// just holds an evaluation result, but can be of a variety of types, e.g. string literal, quoted list, function, etc.
public class Value<T> {
    protected final T value;
    private final ValueType type;

    public Value(T value, ValueType type) {
        this.value = value;
        this.type = type;
    }

    public static Value<?> nil() {
        Symbol nil = Symbols.internSymbol("nil");
        return new SymbolValue(nil);
    }

    public static Value<?> t() {
        Symbol t = Symbols.internSymbol("t");
        return new SymbolValue(t);
    }

    // no env lookup, just literal value
    public static Value<?> of(String value) {
        return atomToValueNoLookup(value);
    }

    public static <T> Value<T> of(T value,
                                  ValueType valueType) {
        return new Value<>(value, valueType);
    }

    public T getValue() {
        return value;
    }

    public ValueType getType() {
        return type;
    }

    // TODO duplicate code (also in BindingEvaluator, which needs to not take an Atom,
    //      just a string and not take an env, just use Symbols
    static Value<?> atomToValueNoLookup(String valueFromAtom) {
        if(valueFromAtom.startsWith(":")) {
            // keyword symbol - a literal symbol which evaluates to itself
            Symbol symbol = Symbols.internSymbol(valueFromAtom);
            return new SymbolValue(symbol);
        }
        else if(valueFromAtom.startsWith("\"") && valueFromAtom.endsWith("\"")) {
            String stringWithoutQuotes = valueFromAtom.substring(1, valueFromAtom.length()-1);
            return new StringValue(stringWithoutQuotes);
        }
        else if(isNumeric(valueFromAtom)) {
            int intValue = Integer.parseInt(valueFromAtom);
            return new IntegerValue(intValue);
        }
        else {
            // treat as symbol
            Symbol symbol = Symbols.internSymbol(valueFromAtom);
            return new SymbolValue(symbol);
        }
    }

    private static boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value<?> value1 = (Value<?>) o;
        return Objects.equals(value, value1.value) && type == value1.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, type);
    }

    public String toString() {
        return value.toString();
    }
}
