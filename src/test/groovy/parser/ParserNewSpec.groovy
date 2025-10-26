package parser

import spock.lang.Specification

class ParserNewSpec extends Specification {

    def "parses function"() {
        given:

        when:
        def parseResult = ParserNew.parse(program)

        then:
        expectedFunction == parseResult.form().nodes()[0]

        where:
        program                                        || expectedFunction
        "(add 1 2)"                                    || new Node(NodeType.FUNCTION, "add", [])
        "((lambda (x) (+ x 1)) 1)"                     || new Node(NodeType.FUNCTION, "(lambda (x) (+ x 1))", [])
        "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"  || new Node(NodeType.FUNCTION, "( (lambda (x) (lambda (y) (+ x y))) 10)", [new Node(NodeType.FUNCTION, " (lambda (x) (lambda (y) (+ x y)))", []), new Node(NodeType.ATOM, "10", List.of())])
    }

    def "parses arguments"() {
        given:

        when:
        def parseResult = ParserNew.parse(program)

        then:
        expectedArguments == parseResult.form().nodes()[1..-1]

        where:
        program                                        || expectedArguments
        "(add 1 2)"                                    || [new Node(NodeType.ATOM, "1", []), new Node(NodeType.ATOM, "2", [])]
        "((lambda (x) (+ x 1)) 1)"                     || [new Node(NodeType.ATOM, "1", [])]
        "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"  || [new Node(NodeType.ATOM, "5", [])]
    }


}
