package evaluator

import evaluator.env.Environment
import evaluator.env.Symbols
import spock.lang.Specification
import value.Value
import value.ValueType

class SetqSpec extends Specification {

    def "basic test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def globalDef = "(defvar *x* 1)"
        def setq = """
            (setq *x* 2)
        """

        when:
        interpreter.interpret(globalDef)
        Value<?> result = interpreter.interpret(setq)

        then:
        result.getType() == ValueType.INTEGER_LITERAL
        def updatedValue = env.get(Symbols.internSymbol("*x*"))
        updatedValue.get().value == 2
    }

    def "cd db test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def globalDef = "(defvar *db* nil)"
        def setq = """
            (setq *db* (cons 1 *db*))
        """

        when:
        interpreter.interpret(globalDef)
        Value<?> result = interpreter.interpret(setq)

        then:
        result.getType() == ValueType.CONS_CELL
    }
}
