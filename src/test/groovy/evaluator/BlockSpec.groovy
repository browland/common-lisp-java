package evaluator

import evaluator.env.Environment
import evaluator.special.ReturnFromException
import spock.lang.Specification
import value.Value

class BlockSpec extends Specification {

    def "simple no return-from"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = "(block test 1 2 3)"

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 3

    }

    def "simple return-from"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                (return-from foo 42)
             99)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 42
    }

    def "conditional return-from"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                (if t
                    (return-from foo 10)
                    20)
                30)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 10
    }

    def "nested blocks with inner return-from"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block outer
                (block inner
                    (return-from inner 5))
                7)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 7
    }

    def "nested blocks with propagated return-from"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block outer
                (block inner
                    (return-from outer 99)
                    1)
                2)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 99
    }

    def "block name shadowing"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                (block foo
                    (return-from foo 1))
                    2)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 2
    }

    def "return-from skips side-effects"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (let ((x 0))
                (block foo
                    (setq x 1)
                    (return-from foo x)
                    (setq x 2))
                    x)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 1
    }

    def "return-from works from inside lambda application"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                ((lambda ()
                    (return-from foo 123)))
                0)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 123
    }

    def "return-from works from inside nested lambda application"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                ((lambda ()
                    ((lambda ()
                        (return-from foo 77))))))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 77
    }

    def "return-from earliest wins"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                (return-from foo 1)
                (return-from foo 2))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 1
    }

    def "return-from default return value of nil"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                (return-from foo))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result == Value.nil()
    }

    def "block name can be nil"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block nil
                (return-from nil 8))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 8
    }

    def "cannot use block name outside scope even when captured by closure"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
        (let ((f (block foo
                (lambda () (return-from foo 9)))))
            (funcall f))
        """

        when:
        interpreter.interpret(program)

        then:
        thrown ReturnFromException
    }

}
