package evaluator

import spock.lang.Specification
import value.Value

class IfTest extends Specification {
    def "true test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def program = "(if (+ 1 1) (+ 1 2) (+ 1 3))";

        when:
        Value<?> result = evaluator.evaluate(program, env)

        then:
        result.value() == 3
    }

    def "false test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def program = "(if nil (+ 1 2) (+ 1 3))";

        when:
        Value<?> result = evaluator.evaluate(program, env)

        then:
        result.value() == 4
    }
}
