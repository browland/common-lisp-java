package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value
import value.ValueType

class MacroSpec extends Specification {
    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new Environment()

        def macroDef = """
           (defmacro testing (x y)
             `(+ ,x ,y))
        """

        when:
        evaluator.evaluate(macroDef, env)
        Value<?> result = evaluator.evaluate("(testing 1 2)", env)

        then:
        result.getValue() == 3
    }

    def "variadic args test"() {
        given:
        def evaluator = new Evaluator()
        def env = new Environment()

        def macroDef = """
            (defmacro testing (x &rest rest) 
                `(+ ,x (car ',rest)) 
            )
        """

        when:
        evaluator.evaluate(macroDef, env)
        Value<?> result = evaluator.evaluate("(testing 1 2 3)", env)

        then:
        result.getValue() == 3
    }

    def "push macro test"() {
        given:
        def evaluator = new Evaluator()
        def env = new Environment()

        def globalDef = "(defvar *db* nil)"
        def macroDef = """
            (defmacro push (item place)
                `(setf ,place (cons ,item ,place)))
        """
        def push = "(push 1 *db*)"

        when:
        evaluator.evaluate(globalDef, env)
        evaluator.evaluate(macroDef, env)
        Value<?> result = evaluator.evaluate(push, env)

        then:
        result.getType() == ValueType.CONS_CELL

        // todo additional tests; can't see the updated global var until we fix global handling (as it doesn't ripple up)
    }
}
