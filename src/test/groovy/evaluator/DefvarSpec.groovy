package evaluator

import spock.lang.Specification
import value.Value;

class DefvarSpec extends Specification {
    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def program = "(defvar *db* nil)"

        when:
        Value<?> result = evaluator.evaluate(program, env)

        then:
        result.value() == "*db*"
        env.get("*db*") == Value.nil()
    }
}
