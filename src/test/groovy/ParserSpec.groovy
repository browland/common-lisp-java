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
}
