import spock.lang.Specification

class InterpreterSpec extends Specification {
    def "addition works ok"() {
        when:
        def result = Interpreter.eval(program)

        then:
        result == expectedResult

        where:
        program              || expectedResult
        "(add 1 2)"          || "3"
        "(add 1 (add 1 2))"  || "4"
        "(add 1 -1)"         || "0"
    }

    def "eq works ok"() {
        when:
        def result = Interpreter.eval(program)

        then:
        result == expectedResult

        where:
        program            || expectedResult
        "(eq 1 2)"         || "false"
        "(eq 1 1)"         || "true"
        "(eq 2 (add 1 1))" || "true"
    }

    def "if works ok"() {
        when:
        def result = Interpreter.eval(program)

        then:
        result == expectedResult

        where:
        program             || expectedResult
        "(if true 1 2)"     || "1"
        "(if false 1 2)"    || "2"
        "(if (eq 1 1) 1 2)" || "1"
        "(if (eq 1 2) 1 2)" || "2"
    }
}
