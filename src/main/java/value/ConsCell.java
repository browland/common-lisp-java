package value;

import java.util.List;

public record ConsCell(Value<?> car,
                       Value<?> cdr) {

    public static ConsCell fromJavaList(List<Value<?>> javaList) {
        // iterate over the list in reverse order, creating cons cells
        Value<?> cdr = Value.nil();
        int i = javaList.size() - 1;
        Value<?> car = javaList.get(i);

        while (--i >= 0) {
            ConsCell consCell = new ConsCell(car, cdr);

            car = javaList.get(i);
            cdr = new Value<>(consCell, ValueType.CONS_CELL);
        }

        return new ConsCell(car, cdr);
    }
}
