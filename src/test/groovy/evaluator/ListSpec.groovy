package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value

class ListSpec extends Specification {
    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new Environment()

        def defVar = "(defvar l (list 1 2 3))";

        when:
        evaluator.evaluate(defVar, env)
        Value<?> result = evaluator.evaluate("(car l)", env)

        then:
        result.getValue() == 1
    }

    def "list elements requiring evaluation test"() {
        given:
        def evaluator = new Evaluator()
        def env = new Environment()

        def defVar = "(defvar l (list (add 1 2) 2 3))";

        when:
        evaluator.evaluate(defVar, env)
        Value<?> result = evaluator.evaluate("(car l)", env)

        then:
        result.getValue() == 3
    }
}
