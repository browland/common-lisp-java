package function

import evaluator.env.Environment
import spock.lang.Specification
import value.ConsCell
import value.Value
import value.ValueType

class ConsSpec extends Specification {

    def "single element cons"() {
        given:
        Cons cons = new Cons();

        Value<String> car = new Value<>("head of list", ValueType.STRING_LITERAL);
        Value<?> cdr = Value.nil();

        when:
        Value<?> consCellValue = cons.apply(List.of(car, cdr), new Environment());

        then:
        ConsCell consCell = consCellValue.value() as ConsCell;
        consCell.car() == car
        consCell.cdr() == Value.nil()
    }

    def "two element cons"() {
        given:
        Cons cons = new Cons();

        Value<String> car = new Value<>("head of list", ValueType.STRING_LITERAL);
        Value<?> cdr = Value.nil();
        ConsCell existingCell = new ConsCell(car, cdr)
        Value<?> existingCellValue = new Value<?>(existingCell, ValueType.CONS_CELL)

        Value<String> newCar = new Value<>("new head of list", ValueType.STRING_LITERAL);

        when:
        Value<?> newConsCellValue = cons.apply(List.of(newCar, existingCellValue), new Environment());

        then:
        ConsCell newConsCell = newConsCellValue.value() as ConsCell;
        newConsCell.car() == newCar
        newConsCell.cdr() == existingCellValue
    }
}
