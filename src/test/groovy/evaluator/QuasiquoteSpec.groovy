package evaluator

import evaluator.env.Environment
import evaluator.env.Symbols
import spock.lang.Specification
import value.ConsCell
import value.ValueType

class QuasiquoteSpec extends Specification {
    def "basic test with no unquote"() {
        given:
        def interpreter = new Interpreter()

        def program = "`(1 2 3)"

        when:
        var result = interpreter.interpret(program)

        then:
        result.type == ValueType.CONS_CELL

        var consCell = result.value as ConsCell
        consCell.car().value == 1

        var nextConsCell = consCell.cdr().value as ConsCell
        nextConsCell.car().value == 2

        var lastConsCell = nextConsCell.cdr().value as ConsCell
        lastConsCell.car().value == 3
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
        result.type == ValueType.CONS_CELL

        var consCell = result.value as ConsCell
        consCell.car().value == 1

        var nextConsCell = consCell.cdr().value as ConsCell
        nextConsCell.car().value == 2

        var lastConsCell = nextConsCell.cdr().value as ConsCell
        lastConsCell.car().value == 3
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
        // Expect result:
        // (+ 1 (car (quote (2 3))))
        result.type == ValueType.CONS_CELL

        var consCell = result.value as ConsCell
        consCell.car().value == Symbols.internSymbol("+")

        var nextConsCell = consCell.cdr().value as ConsCell
        nextConsCell.car().value == 1

        var outerCarConsCell = nextConsCell.cdr().value as ConsCell
        var carConsCell = outerCarConsCell.car().value as ConsCell
        carConsCell.car().value == Symbols.internSymbol("car")

        var outerQuoteConsCell = carConsCell.cdr().value as ConsCell
        var quoteConsCell = outerQuoteConsCell.car().value as ConsCell
        quoteConsCell.car().value == Symbols.internSymbol("quote")

        var outerNumListConsCell = quoteConsCell.cdr().value as ConsCell
        var numListConsCell = outerNumListConsCell.car().value as ConsCell
        numListConsCell.car().value == 2

        var lastNumConsCell = numListConsCell.cdr().value as ConsCell
        lastNumConsCell.car().value == 3
    }
}
