package evaluator

import evaluator.env.Environment
import spock.lang.Specification

class LetSpec extends Specification {

    def "two bindings"() {
        given:
        def interpreter = new Interpreter()
        def program = """
          (let ((x 2)
                (y 3))
                (+ x y))"""

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 5
    }

    def "nested let"() {
        given:
        def interpreter = new Interpreter()
        def program = """
          (let ((x 5))
            (let ((x 2)
                  (y 3))
                  (+ x y)))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 5
    }

    def "shadowing"() {
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

    def "evaluates multiple forms"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = "(let ((x 1)) (+ x 1) (+ x 2))"

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 3
    }
}
