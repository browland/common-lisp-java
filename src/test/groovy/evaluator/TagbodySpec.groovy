package evaluator

import evaluator.env.Environment
import spock.lang.Specification

class TagbodySpec extends Specification {

    def "should not evaluate a tag"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = "(tagbody test (+ 1 1))"

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 2
    }

    def "evaluates multiple forms and returns value of last one"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = "(tagbody test (+ 1 1) (+ 1 2))"

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 3
    }

    def "simple loop"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = "(let ((x 0)) (tagbody inc (setq x (+ x 1)) (if (< x 10) (go inc))) x)"

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 10
    }
}
