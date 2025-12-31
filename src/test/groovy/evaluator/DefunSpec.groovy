package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value

class DefunSpec extends Specification {
    def "simple test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def functionDef = """
           (defun f (x y)
             (+ x y))
        """

        when:
        interpreter.interpret(functionDef)
        Value<?> result = interpreter.interpret("(f 1 2)")

        then:
        result.getValue() == 3
    }

    def "variadic args test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def functionDef = """
           (defun f (x &rest others)
             (+ x (car others)))
        """

        when:
        interpreter.interpret(functionDef)
        Value<?> result = interpreter.interpret("(f 1 2)")

        then:
        result.getValue() == 3
    }
}
