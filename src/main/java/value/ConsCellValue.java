package value;

import syntaxtree.Atom;
import syntaxtree.RList;

import java.util.List;

public class ConsCellValue extends Value<ConsCell> {
    public ConsCellValue(ConsCell value) {
        super(value, ValueType.CONS_CELL);
    }

    public static ConsCellValue fromJavaList(RList readerList) {
        return fromJavaList(readerList.nodes());
    }

    public static ConsCellValue fromJavaList(List<?> javaList) {
        // iterate over the list in reverse order, creating cons cells
        Value<?> cdr = Value.nil();
        int i = javaList.size() - 1;

        Value<?> car = toValue(javaList.get(i));

        while (--i >= 0) {
            ConsCell consCell = new ConsCell(car, cdr);

            car = toValue(javaList.get(i));
            cdr = new ConsCellValue(consCell);
        }

        ConsCell consCell = new ConsCell(car, cdr);
        return new ConsCellValue(consCell);
    }

    private static Value<?> toValue(Object object) {
        return switch (object) {
            case Value<?> value1 -> value1;
            case Atom atom -> Value.of(atom.value());
            case RList rlist -> fromJavaList(rlist);
            case null, default ->
                    throw new UnsupportedOperationException("unsupported type to build cons cell " + object);
        };
    }
}
