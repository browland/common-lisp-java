package evaluator

import spock.lang.Specification
import value.Value

class DefunSpec extends Specification {
    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def functionDef = """
           (defun f (x y)
             (+ x y))
        """

        when:
        evaluator.evaluate(functionDef, env)
        Value<?> result = evaluator.evaluate("(f 1 2)", env)

        then:
        result.value() == 3
    }

    def "variadic args test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def functionDef = """
           (defun f (x &rest others)
             (+ x (car others)))
        """

        when:
        evaluator.evaluate(functionDef, env)
        Value<?> result = evaluator.evaluate("(f 1 2)", env)

        then:
        result.value() == 3
    }
}
