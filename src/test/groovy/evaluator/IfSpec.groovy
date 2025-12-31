package evaluator

import spock.lang.Specification
import value.Value

class IfSpec extends Specification {
    def "true test"() {
        given:
        def interpreter = new Interpreter()

        def program = "(if (+ 1 1) (+ 1 2) (+ 1 3))";

        when:
        Value<?> result = interpreter.interpret(program)

        then:
        result.getValue() == 3
    }

    def "false test"() {
        given:
        def interpreter = new Interpreter()

        def program = "(if nil (+ 1 2) (+ 1 3))";

        when:
        Value<?> result = interpreter.interpret(program)

        then:
        result.getValue() == 4
    }
}
