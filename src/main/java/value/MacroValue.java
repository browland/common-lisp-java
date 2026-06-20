package value;

public final class MacroValue extends Value<Macro> {
    public MacroValue(Macro macro) {
        super(macro, ValueType.MACRO);
    }
}
