package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value;

class DefvarSpec extends Specification {
    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new Environment()

        def program = "(defvar *db* nil)"

        when:
        Value<?> result = evaluator.evaluate(program, env)

        then:
        result.getValue() == "*db*"
        env.get("*db*").get() == Value.nil()
    }
}
