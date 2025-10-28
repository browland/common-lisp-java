package interpreter


import spock.lang.Specification

class InterpreterNewSpec extends Specification {

    def "handles function application"() {
        given:
        when:
        String result = InterpreterNew.interpret(program)

        then:
        result == expectedResult

        where:
        program                                        || expectedResult
        "(add 1 2)"                                    || "3"
        "((lambda (x) (+ x 1)) 1)"                     || "2"
        "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"  || "15"
    }
}
