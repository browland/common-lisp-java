package function

import evaluator.env.Environment;
import spock.lang.Specification
import value.ConsCell
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
        ConsCell result = lispListValue.getValue() as ConsCell
        result == expectedConsCell
    }

    def "two element list"() {
        given:
        ListFunction listFunction = new ListFunction()

        when:
        Value<?> value1 = new Value<>("value 1", ValueType.STRING_LITERAL);
        Value<?> value2 = new Value<>("value 2", ValueType.STRING_LITERAL);
        Value<?> lispListValue = listFunction.apply(List.of(value1, value2), new Environment())

        then:
        ConsCell result = lispListValue.getValue() as ConsCell
        result.car() == value1

        ConsCell secondConsCell = result.cdr().getValue() as ConsCell
        secondConsCell.car() == value2
    }
}
