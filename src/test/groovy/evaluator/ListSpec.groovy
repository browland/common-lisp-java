package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value

class ListSpec extends Specification {
    def "simple test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter()

        def defVar = "(defvar l (list 1 2 3))";

        when:
        interpreter.interpret(defVar)
        Value<?> result = interpreter.interpret("(car l)")

        then:
        result.getValue() == 1
    }

    def "list elements requiring evaluation test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter()

        def defVar = "(defvar l (list (add 1 2) 2 3))";

        when:
        interpreter.interpret(defVar)
        Value<?> result = interpreter.interpret("(car l)")

        then:
        result.getValue() == 3
    }
}
