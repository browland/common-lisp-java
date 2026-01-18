package evaluator

import evaluator.env.Environment
import evaluator.special.ReturnFromException
import spock.lang.Specification

class FuncallSpec extends  Specification {
    def "simple case"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (funcall (lambda () (+ 1 1)))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 2
    }

    def "with args"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (funcall (lambda (x) (+ x 1)) 42)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 43
    }

    def "from variable"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (let ((f (lambda () (+ 2 1))))
                (funcall f))
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 3
    }

    def "does not use function namespace"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def defun = """
            (defun f () 10)
        """

        def invocation = """
            (let ((f (lambda () 20)))
                (funcall f))
        """

        when:
        interpreter.interpret(defun)
        def result = interpreter.interpret(invocation)

        then:
        result.value == 20
    }

    def "with return-from"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (block foo
                (funcall (lambda () (return-from foo 9)))
                0)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 9
    }

    def "multiple args"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (funcall (lambda (a b) (+ a b)) 2 3)
        """

        when:
        def result = interpreter.interpret(program)

        then:
        result.value == 5
    }

    def "errors when captured block name referenced outside scope"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)
        def program = """
            (let ((f (block foo
                        (lambda () (return-from foo 5)))))
                (funcall f))
        """

        when:
        interpreter.interpret(program)

        then:
        thrown ReturnFromException
    }
}
