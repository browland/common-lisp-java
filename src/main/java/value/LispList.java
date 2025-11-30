package value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LispList {
    private final ConsCell headConsCell;

    public LispList(List<Value<?>> list) {
        // iterate over the list in reverse order, creating cons cells
        Value<?> cdr = Value.nil();
        int i = list.size()-1;
        Value<?> car = list.get(i);

        while(--i >= 0) {
            ConsCell consCell = new ConsCell(car, cdr);

            car = list.get(i);
            cdr = new Value<>(consCell, ValueType.CONS_CELL);
        }

        headConsCell = new ConsCell(car, cdr);
    }

    public ConsCell getHeadConsCell() {
        return headConsCell;
    }

    public Map<Value<?>, Value<?>> getPropertyList() {
        return null;
    }
}
