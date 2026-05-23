package value;

public class SymbolValue extends Value<Symbol> {
    public SymbolValue(Symbol value) {
        super(value, ValueType.SYMBOL);
    }

    public String toString() {
        return value.name();
    }
}
