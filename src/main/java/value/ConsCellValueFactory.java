package value;

import syntaxtree.Atom;
import syntaxtree.RList;

import java.util.List;

public class ConsCellValueFactory {

    public static ConsCellValue fromRList(RList readerList) {
        if(readerList.nodes().isEmpty()) {
            return new ConsCellValue(ConsCell.empty());
        }
        return readerList.improperList() ? improperListFromJavaList(readerList.nodes()) : fromJavaList(readerList.nodes());
    }

    public static ConsCellValue improperListFromJavaList(List<?> javaList) {
        if(javaList.size() != 2) {
            throw new RuntimeException("Evaluating improper list but != 2 elements; not implemented properly");
        }

        Value<?> car = toValue(javaList.get(0));
        Value<?> cdr = toValue(javaList.get(1));

        ConsCell consCell = new ConsCell(car, cdr);
        return new ConsCellValue(consCell);
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
            case RList rlist -> fromRList(rlist);
            case null, default ->
                    throw new UnsupportedOperationException("unsupported type to build cons cell " + object);
        };
    }
}
