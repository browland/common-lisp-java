import spock.lang.Specification

class InterpreterSpec extends Specification {

    def "overall program forms work ok"() {
        when:
        def result = Interpreter.eval(program, Map.of())

        then:
        result == expectedResult

        where:
        program        || expectedResult
        "1"            || "1"
        "(add 1 2) 1"  || "1"
    }

    def "add works ok"() {
        when:
        def result = Interpreter.eval(program, Map.of())

        then:
        result == expectedResult

        where:
        program              || expectedResult
        "(add 1 2)"          || "3"
        "(add 1 (add 1 2))"  || "4"
        "(add 1 -1)"         || "0"
    }

    def "add failure cases"() {
        when:
        Interpreter.eval(program, Map.of())

        then:
        def e = thrown(UndefinedVariableException)
        e.message == errorMessage

        where:
        program      || errorMessage
        "(add x 1)"  || "Undefined variable x"
    }

    def "eq works ok"() {
        when:
        def result = Interpreter.eval(program, Map.of())

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
        def result = Interpreter.eval(program, Map.of())

        then:
        result == expectedResult

        where:
        program             || expectedResult
        "(if true 1 2)"     || "1"
        "(if false 1 2)"    || "2"
        "(if (eq 1 1) 1 2)" || "1"
        "(if (eq 1 2) 1 2)" || "2"
    }

    def "less works ok"() {
        when:
        def result = Interpreter.eval(program, Map.of())

        then:
        result == expectedResult

        where:
        program      || expectedResult
        "(less 2 3)" || "true"
        "(less 3 2)" || "false"
    }

    def "let works ok"() {
        when:
        def result = Interpreter.eval(program, Map.of())

        then:
        result == expectedResult

        where:
        program                                                      || expectedResult
        "(let ((x 1)) x)"                                            || "1"
        "(let ((x 1) (y 2)) (add x y))"                              || "3"
        "(let ((x 2)) (let ((y (add x 3))) y))"                      || "5"
        "(let ((x 1)) (let ((x 2)) x))"                              || "2"
        "(let ((x 1) (y 2)) (add x y) (add y 3))"                    || "5"
        "(let ((a 10)) (let ((b (add a 5))) (add a b)))"             || "25"
        "(let ((x (let ((y 2)) (add y 3)))) (add x 1))"              || "6"
        "(let ((x 1) (y 2)) (if (less x y) (add y 10) (add x 10)))"  || "12"
    }

    def "let failure cases"() {
        when:
        Interpreter.eval(program, Map.of())

        then:
        def e = thrown(UndefinedVariableException)
        e.message == errorMessage

        where:
        program                   || errorMessage
        "(let ((x (add x 1))) x)" || "Undefined variable x"
    }
}
