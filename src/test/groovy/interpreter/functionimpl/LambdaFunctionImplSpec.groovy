package interpreter.functionimpl

import parser.Node
import parser.NodeType
import parser.FormTreeBuilder
import spock.lang.Specification

class LambdaFunctionImplSpec extends Specification {

    def "should apply lambda"() {
        given:
        def parsedFunctionForm = FormTreeBuilder.parse(functionForm).form()
        def impl = new LambdaFunctionImpl(parsedFunctionForm);
//        def nodes = parsedFunctionForm.nodes()

        def argNodes = List.of(new Node(NodeType.OPERAND, arguments, "don't think this matters", []))

        when:
        def result = impl.apply(argNodes);

        then:
        result == expectedResult

        where:
        functionForm                          ||  arguments ||  expectedResult
        " (lambda (x) (lambda (y) (+ x y)))"  ||  "10"      || "(lambda (y) (+ 10 y))"
    }
}
