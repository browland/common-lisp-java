package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value

class PrognSpec extends Specification {

    def "empty progn has value nil"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (progn)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result == Value.nil()
    }

    def "multi-form progn returns last value"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (progn 1 2 3)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 3
    }

    def "forms have side-effects"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (progn (setq x 1) x)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 1
    }

    def "non-local exit works"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo (progn (return-from foo 5) 9))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 5
    }

}
