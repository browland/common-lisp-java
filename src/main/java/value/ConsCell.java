package value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public record ConsCell(Value<?> car,
                       Value<?> cdr) implements Iterable<Value<?>> {

    public static ConsCell fromValue(Value<?> value) {
        return new ConsCell(value, Value.nil());
    }

    public ConsCell push(Value<?> newHead) {
        return new ConsCell(newHead, new ConsCellValue(this));
    }

    public String toString() {
        List<Value<?>> valueList = new ArrayList<>();
        for(Value<?> nextValue : this) {
            valueList.add(nextValue);
        }

        return valueList.stream()
                .map(Value::toString)
                .collect(Collectors.joining(" ", "(", ")"));
    }

    @Override
    public Iterator<Value<?>> iterator() {
        return new ConsIterator(this);
    }

    @Override
    public void forEach(Consumer<? super Value<?>> action) {
        Iterable.super.forEach(action);
    }

    static class ConsIterator implements Iterator<Value<?>> {
        private ConsCell currentCons;


        public ConsIterator(ConsCell currentCons) {
            this.currentCons = currentCons;
        }

        @Override
        public boolean hasNext() {
            return currentCons != null;
        }

        @Override
        public Value<?> next() {
            ConsCell savedCons = currentCons;
            if(currentCons.cdr() instanceof ConsCellValue nextConsCellValue) {
                currentCons = nextConsCellValue.getValue();
            }
            else if(currentCons.cdr().equals(Value.nil())) {
                currentCons = null;
            }
            else {
                throw new IllegalStateException("cdr is not either ConsCellValue or nil");
            }

            return savedCons.car();
        }
    }
}
