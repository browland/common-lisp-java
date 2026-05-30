package evaluator


import spock.lang.Specification
import value.IntegerValue
import value.Value
import value.ValuesValue

class FloorSpec extends Specification {
    def "simple test"() {
        given:
        def interpreter = new Interpreter()

        def program = "(floor 3 2)";

        when:
        Value<?> result = interpreter.interpret(program)

        then:
        result == new ValuesValue(List.of(new IntegerValue(1), new IntegerValue(1)))
    }
}
