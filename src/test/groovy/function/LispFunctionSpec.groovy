package function

import evaluator.env.Environment;
import spock.lang.Specification
import value.ConsCell
import value.LispList
import value.Value
import value.ValueType;

class LispFunctionSpec extends Specification {

    def "single element list"() {
        given:
        ListFunction listFunction = new ListFunction()

        when:
        Value<?> singleValue = new Value<>("head", ValueType.STRING_LITERAL);
        Value<?> lispListValue = listFunction.apply(List.of(singleValue), new Environment())

        then:
        ConsCell expectedConsCell = new ConsCell(singleValue, Value.nil())
        LispList lispList = lispListValue.getValue() as LispList
        lispList.getHeadConsCell() == expectedConsCell
    }

    def "two element list"() {
        given:
        ListFunction listFunction = new ListFunction()

        when:
        Value<?> value1 = new Value<>("value 1", ValueType.STRING_LITERAL);
        Value<?> value2 = new Value<>("value 2", ValueType.STRING_LITERAL);
        Value<?> lispListValue = listFunction.apply(List.of(value1, value2), new Environment())

        then:
        LispList lispList = lispListValue.getValue() as LispList
        lispList.getHeadConsCell().car() == value1

        ConsCell secondConsCell = lispList.getHeadConsCell().cdr().getValue() as ConsCell
        secondConsCell.car() == value2
    }
}
