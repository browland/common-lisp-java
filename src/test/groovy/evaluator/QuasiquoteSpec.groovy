package evaluator

import evaluator.env.Environment
import spock.lang.Specification

class QuasiquoteSpec extends Specification {
    def "basic test with no unquote"() {
        given:
        def interpreter = new Interpreter()

        def program = "`(1 2 3)"

        when:
        var result = interpreter.interpret(program)

        then:
        result.toString() == "(1 2 3)"
    }

    def "with unquote"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def defVar = "(defvar x 2)"
        def program = "`(1 ,x 3)"

        when:
        interpreter.interpret(defVar)
        var result = interpreter.interpret(program)

        then:
        result.toString() == "(1 2 3)"
    }

    def "embedded unquote"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def defVar1 = "(defvar x 1)"
        def defVar2 = "(defvar rest (list 2 3))"
        def program = "`(+ ,x (car ',rest))"

        when:
        interpreter.interpret(defVar1)
        interpreter.interpret(defVar2)
        var result = interpreter.interpret(program)

        then:
        result.toString() == "(+ 1 (car (quote (2 3))))"
    }

    def "quasiquoted atom"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def program = "`1"

        when:
        var result = interpreter.interpret(program)

        then:
        result.toString() == "1"
    }

    def "unquote splicing"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def defVar = "(defvar x '(1 2 3))"
        def program = "`(1 ,@x 3)"

        when:
        interpreter.interpret(defVar)
        var result = interpreter.interpret(program)

        then:
        result.toString() == "(1 1 2 3 3)"
    }
}
