package value;

public final class ConsCellValue extends Value<ConsCell> {
    public ConsCellValue(ConsCell value) {
        super(value, ValueType.CONS_CELL);
    }

    public String toString() {
        return value.toString();
    }

    public boolean equals(ConsCellValue other) {
        return this.getValue().equals(other.getValue());
    }
}
