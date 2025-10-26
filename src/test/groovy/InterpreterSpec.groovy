import spock.lang.Specification

class InterpreterSpec extends Specification {

    def "overall program forms work ok"() {
        when:
        def result = Interpreter.eval(program)

        then:
        result == expectedResult

        where:
        program        || expectedResult
        "1"            || "1"
        "(add 1 2) 1"  || "1"
    }

    def "add works ok"() {
        when:
        def result = Interpreter.eval(program)

        then:
        result == expectedResult

        where:
        program              || expectedResult
        "(add 1 2)"          || "3"
        "(+ 1 2)"            || "3"
        "(add 1 (add 1 2))"  || "4"
        "(add 1 -1)"         || "0"
    }

    def "add failure cases"() {
        when:
        Interpreter.eval(program)

        then:
        def e = thrown(UndefinedVariableException)
        e.message == errorMessage

        where:
        program      || errorMessage
        "(add x 1)"  || "Undefined variable x"
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

    def "less works ok"() {
        when:
        def result = Interpreter.eval(program)

        then:
        result == expectedResult

        where:
        program      || expectedResult
        "(less 2 3)" || "true"
        "(less 3 2)" || "false"
    }

    def "let works ok"() {
        when:
        def result = Interpreter.eval(program)

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
        Interpreter.eval(program)

        then:
        def e = thrown(UndefinedVariableException)
        e.message == errorMessage

        where:
        program                   || errorMessage
        "(let ((x (add x 1))) x)" || "Undefined variable x"
    }

    def "lambda works ok"() {
        when:
        def result = Interpreter.eval(program)

        then:
        result == expectedResult

        where:
        program                                         || expectedResult
//        "((lambda (x) (+ x 1)) 4)"                      || "5"
//        "((lambda (x y) (+ x y)) 3 7)"                  || "10"
//        "((lambda (x) ((lambda (y) (+ x y)) 5)) 3)"     || "8"
        "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"   || "15"
    }

    // in this form:
    // (( (lambda (x) (lambda (y) (+ x y))) 10) 5)
    // the top level form means "apply this function to argument 5"
    // so, apply (stuff_expressed_as_a_list) to 5
    // what is that stuff?  It's:
    // ( (lambda (x) (lambda (y) (+ x y))) 10)
    // what's this saying?  It's another case of: express this function to 10, e.g:
    // ((stuff_in_list) 10)
    // what's the stuff?
    // (lambda (x) (lambda (y) (+ x y)))
    // this is saying, it's just a function - (x) is the arg list, then there's a body, then there's no binding!
    // so because it's an 'incomplete' lambda, it needs keeping as a function and can only be eval'd in terms of the
    // outer function
    //
    // what we need ... ?
    // the function (on the left) can be 'derived' through levels of composition, e.g. collecting vars and passing them
    // down to the eventual thing which can be evaluated
    // so we need to handle our 'function part' (first expression at top level) being arbitrarily deep, and that these
    // 'intermediate functions' need to be passed back as first-class things in their own right.
    // function composition
    // one function has to be able to transform another? So we end up with just one simpler function?
    // as, ultimately we need to evaluate something simpler in the end
    // ( (lambda (x) (lambda (y) (+ x y))) 10) with a binding of can be composed into:
    // (lambda (x, y) (+ x y)
    // with a binding of x=10
}

