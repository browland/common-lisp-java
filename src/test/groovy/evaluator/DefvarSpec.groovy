package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Symbol
import value.SymbolValue
import value.Value;

class DefvarSpec extends Specification {
    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new Environment()

        def program = "(defvar *db* nil)"

        when:
        SymbolValue result = evaluator.evaluate(program, env)

        then:
        result.getValue() == new Symbol("*db*")
        env.get(result.getValue()).get() == Value.nil()
    }
}
