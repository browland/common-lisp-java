package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value

class FLetSpec extends Specification {
    def "shadows global function definition"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def functionDef = "(defun f () 1)"
        def functionInvocation = """
            (flet ((f () 2))
                (f))
        """

        when:
        interpreter.interpret(functionDef)
        Value<?> result = interpreter.interpret(functionInvocation)

        then:
        result.getValue() == 2
    }

    def "with multiple definitions a binding can reference another one but not evaluated until call time "() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def flet = """
            (flet ((f () 1)
                   (g () (f)))
                (g))       
        """

        when:
        Value<?> result = interpreter.interpret(flet)

        then:
        result.getValue() == 1
    }

    def "self-referencing binding works but stack overflows"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def flet = """
            (flet ((f () (f)))
                (f))
        """

        when:
        Value<?> result = interpreter.interpret(flet)
        print(result)

        then:
        // we expect "Unknown operator f" as the evaluation of the closure shouldn't be able to see
        // its own binding within its body.
        thrown StackOverflowError
    }

}
