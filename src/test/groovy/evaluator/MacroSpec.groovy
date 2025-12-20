package evaluator

import spock.lang.Specification
import value.Value

class MacroSpec extends Specification {
    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def macroDef = """
           (defmacro testing (x y)
             `(+ ,x ,y))
        """

        when:
        evaluator.evaluate(macroDef, env)
        Value<?> result = evaluator.evaluate("(testing 1 2)", env)

        then:
        result.value() == 3
    }

    def "variadic args test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def macroDef = """
            (defmacro testing (x &rest rest) 
                `(+ ,x (car ',rest)) 
            )
        """

        when:
        evaluator.evaluate(macroDef, env)
        Value<?> result = evaluator.evaluate("(testing 1 2 3)", env)

        then:
        result.value() == 3
    }
}
