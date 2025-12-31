package evaluator

import evaluator.env.Environment
import spock.lang.Specification

class LetSpec extends Specification {

    def "shadowing test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def definition = "(defun test (x) (let ((x 1)) (+ x 1)))"
        def invocation = "(test 2)"

        when:
        interpreter.interpret(definition)
        def result = interpreter.interpret(invocation)

        then:
        result.value == 2
    }
}
