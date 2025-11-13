package parser

import spock.lang.Specification

class FormTreeBuilderSpec extends Specification {

    def "parses operator"() {
        given:

        when:
        def nodes = FormTreeBuilder.parse(program)

        then:
        expectedOperator == nodes[0]

        where:
        program                                        || expectedOperator
        "(add 1 2)"                                    || new Node(NodeType.OPERATOR, "add", "(add 1 2)", [])
        "((lambda (x) (+ x 1)) 1)"                     || new Node(NodeType.OPERATOR, "(lambda (x) (+ x 1))", "((lambda (x) (+ x 1)) 1)", [])
        "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"  || new Node(NodeType.OPERATOR, "( (lambda (x) (lambda (y) (+ x y))) 10)", "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)", [new Node(NodeType.OPERATOR, " (lambda (x) (lambda (y) (+ x y)))", "( (lambda (x) (lambda (y) (+ x y))) 10)", []), new Node(NodeType.OPERAND, "10", "( (lambda (x) (lambda (y) (+ x y))) 10)", [])])
    }

    def "parses operands"() {
        given:

        when:
        def nodes = FormTreeBuilder.parse(program)

        then:
        expectedOperands == nodes[1..-1]

        where:
        program                                        || expectedOperands
        "(add 1 2)"                                    || [new Node(NodeType.OPERAND, "1", "(add 1 2)", []), new Node(NodeType.OPERAND, "2", "(add 1 2)", [])]
        "((lambda (x) (+ x 1)) 1)"                     || [new Node(NodeType.OPERAND, "1", "((lambda (x) (+ x 1)) 1)", [])]
        "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"  || [new Node(NodeType.OPERAND, "5", "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)", [])]
    }
}
