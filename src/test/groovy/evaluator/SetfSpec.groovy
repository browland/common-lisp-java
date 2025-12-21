package evaluator

import spock.lang.Specification
import value.Value
import value.ValueType

class SetfSpec extends Specification {
    def "cd db test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def globalDef = "(defvar *db* nil)"
        def setf = """
            (setf *db* (cons 1 *db*))
        """

        when:
        evaluator.evaluate(globalDef, env)
        Value<?> result = evaluator.evaluate(setf, env)

        then:
        result.type() == ValueType.CONS_CELL
    }

}
