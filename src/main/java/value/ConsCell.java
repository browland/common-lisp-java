package value;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ConsCells are mutable (e.g. via rplaca) so this is not a record, and the fields are mutable.
 */
public class ConsCell implements Iterable<Value<?>> {
    private Value<?> car;
    private Value<?> cdr;

    public ConsCell(Value<?> car, Value<?> cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public static ConsCell fromValue(Value<?> value) {
        return new ConsCell(value, Value.nil());
    }

    public ConsCell push(Value<?> newHead) {
        return new ConsCell(newHead, new ConsCellValue(this));
    }

    public String toString() {
        if((Value.nil().equals(car) && Value.nil().equals(cdr)) || (SymbolValue.nil().equals(car) && SymbolValue.nil().equals(cdr))) {
            return "()";
        }

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
        private Value<?> lastValue;  // for pairs, or improper lists

        public ConsIterator(ConsCell currentCons) {
            this.currentCons = currentCons;
        }

        @Override
        public boolean hasNext() {
            return currentCons != null || lastValue != null;
        }

        @Override
        public Value<?> next() {
            if(lastValue != null) {
                Value<?> savedLastValue = lastValue;
                lastValue = null;
                return savedLastValue;
            }

            ConsCell savedCons = currentCons;
            if(currentCons.cdr().equals(Value.nil())) {
                currentCons = null;
            }
            else if(currentCons.cdr() instanceof ConsCellValue nextConsCellValue) {
                currentCons = nextConsCellValue.getValue();
            }
            else {
                // we're dealing with a pair (improper list)
                lastValue = currentCons.cdr();
                currentCons = null;
            }

            return savedCons.car();
        }
    }

    public Value<?> car() {
        return car;
    }

    public Value<?> cdr() {
        return cdr;
    }

    public void setCar(Value<?> car) {
        this.car = car;
    }

    public ConsCellValue wrap() {
        return new ConsCellValue(this);
    }

    public boolean equals(ConsCell other) {
        Iterator<Value<?>> otherIterator = other.iterator();
        for(Value<?> value : this) {
            boolean same = otherIterator.hasNext() && otherIterator.next().equals(value);
            if(!same) {
                return false;
            }
        }
        return !otherIterator.hasNext();
    }
}
