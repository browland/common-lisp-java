package evaluator

import spock.lang.Specification
import value.ConsCell
import value.ValueType

class QuoteSpec extends Specification {
    def "evaluates quoted list"() {
        given:
        def interpreter = new Interpreter()

        def program = "'(1 2 3)"

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
}
