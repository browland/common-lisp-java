package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value
import value.ValueType

class SetfSpec extends Specification {
    def "cd db test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def globalDef = "(defvar *db* nil)"
        def setf = """
            (setf *db* (cons 1 *db*))
        """

        when:
        interpreter.interpret(globalDef)
        Value<?> result = interpreter.interpret(setf)

        then:
        result.getType() == ValueType.CONS_CELL
    }

}
