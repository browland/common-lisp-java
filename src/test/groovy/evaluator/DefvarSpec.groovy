package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Symbol
import value.SymbolValue
import value.Value;

class DefvarSpec extends Specification {
    def "simple test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def program = "(defvar *db* nil)"

        when:
        SymbolValue result = interpreter.interpret(program) as SymbolValue

        then:
        result.getValue() == new Symbol("*db*")
        env.getVariable(result.getValue()).get() == Value.nil()
    }
}
