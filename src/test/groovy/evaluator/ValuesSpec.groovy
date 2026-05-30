package evaluator


import spock.lang.Specification
import value.IntegerValue
import value.Value
import value.ValuesValue

class ValuesSpec extends Specification {
    def "simple test"() {
        given:
        def interpreter = new Interpreter()

        def program = "(values 1 2 3)";

        when:
        Value<?> result = interpreter.interpret(program)

        then:
        result == new ValuesValue(List.of(new IntegerValue(1), new IntegerValue(2), new IntegerValue(3)))
    }

    def "primary value passed to function"() {
        given:
        def interpreter = new Interpreter()

        def program = "(+ 1 (values 1 2 3))";

        when:
        Value<?> result = interpreter.interpret(program)

        then:
        result == new IntegerValue(2)
    }

    def "atomic evaluation returns primary value"() {
        given:
        def interpreter = new Interpreter()

        def program = "(let ((x (values 1 2 3))) x)";

        when:
        Value<?> result = interpreter.interpret(program)

        then:
        result == new IntegerValue(1)
    }
}
