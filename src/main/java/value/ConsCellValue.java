package value;

import syntaxtree.Atom;
import syntaxtree.Node;
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
            cdr = new Value<>(consCell, ValueType.CONS_CELL);
        }

        ConsCell consCell = new ConsCell(car, cdr);
        return new ConsCellValue(consCell);
    }

    private static Value<?> toValue(Object object) {
        if(object instanceof Value) {
            return (Value<?>)object;
        }
        else if(object instanceof Atom atom) {
            return Value.of(atom.value());
        }
        else if(object instanceof RList rlist) {
            return fromJavaList(rlist);
        }
        else {
            throw new UnsupportedOperationException("unsupported type to build cons cell " + object);
        }
    }
}
