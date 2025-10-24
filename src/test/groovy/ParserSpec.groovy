import spock.lang.Specification

class ParserSpec extends Specification {

    def "extracts top level forms"() {
        when:
        def forms = Parser.extractTopLevelForms(program)

        then:
        Arrays.asList(forms) == expectedForms

        where:
        program                             || expectedForms
        "1"                                 || ["1"]
        "1 2 3"                             || ["1", "2", "3"]
        "(add 1 1)"                         || ["(add 1 1)"]
        "(add 1 1) 1"                       || ["(add 1 1)", "1"]
        "(define x 1) (print x) (add x 2)"  || ["(define x 1)", "(print x)", "(add x 2)"]
    }

//    def "splits expressions for lambda definition"() {
//        // the idea is we've already split the top level forms for the lambda, into the definition and bindings.
//        // we now want to extract the forms from the definition so we can get at the argument list, and the actual
//        // expression to be evaluated, as separate parts.
//        when:
//        def forms = Parser.splitExpressionsAtThisLevel(lambdaDefinition)
//
//        then:
//        Arrays.asList(forms) == expectedForms
//
//        where:
//        lambdaDefinition        || expectedForms
//        "(lambda (x) (+ x 1))"  || ["lambda", "(x)", "(+ x 1)"]
//        "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)" || "test"
//        "( (lambda (x) (lambda (y) (+ x y))) 10)"  || "test"
//        "(lambda (x) (lambda (y) (+ x y)))"  || "test"
//    }
}
