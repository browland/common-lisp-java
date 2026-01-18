package evaluator

import evaluator.env.Environment
import spock.lang.Specification

class FunctionSpec extends Specification {

    def "simple case"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = "(funcall #'(lambda () 1))"

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 1

    }
}
