package parser

import spock.lang.Specification

class ListParserSpec extends Specification {
    def parser = new ListParser()

    def "should parse single-level list"() {
        given:
        def program = "(add 1 2)"

        when:
        def parsedList = parser.parse(program)

        then:
        parsedList.getQuoteType() == QuoteType.NONE

        def nodes = parsedList.getNodes()
        nodes.size() == 3
        nodes[0] == new ListNode("add")
        nodes[1] == new ListNode("1")
        nodes[2] == new ListNode("2")
    }

    def "should parse two-level list"() {
        given:
        def program = "(add 1 (add 1 2))"

        when:
        def parsedList = parser.parse(program)

        then:
        parsedList.getQuoteType() == QuoteType.NONE
        def nodes = parsedList.getNodes()
        nodes.size() == 3
        nodes[0] == new ListNode("add")
        nodes[1] == new ListNode("1")

        def innerListNode = nodes[2]
        def innerParsedList = innerListNode.getParsedList()
        innerParsedList.getQuoteType() == QuoteType.NONE

        def innerNodes = innerParsedList.getNodes()

        innerNodes.size() == 3
        innerNodes[0] == new ListNode("add")
        innerNodes[1] == new ListNode("1")
        innerNodes[2] == new ListNode("2")
    }

    // Example programs used from: https://cs.stanford.edu/people/nick/compdocs/LISP_Examples.pdf

    def "defun example"() {
        given:
        def program = "(defun even (num) (= (mod num 2) 0))"

        when:
        def parsedList = parser.parse(program)

        then:
        def outerListNodes = parsedList.getNodes()
        outerListNodes.size() == 4
        outerListNodes[0] == new ListNode("defun")
        outerListNodes[1] == new ListNode("even")

        def argNodesParsedList = outerListNodes[2].getParsedList()
        argNodesParsedList.getQuoteType() == QuoteType.NONE
        def argNodes = argNodesParsedList.getNodes()
        argNodes.size() == 1
        argNodes[0] == new ListNode("num")

        def definitionParsedList = outerListNodes[3].getParsedList()
        definitionParsedList.getQuoteType() == QuoteType.NONE
        def definitionNodes = definitionParsedList.getNodes()
        definitionNodes.size() == 3
        definitionNodes[0] == new ListNode("=")

        def innerDefinitionParsedList = definitionNodes[1].getParsedList()
        def innerDefinitionNodes = innerDefinitionParsedList.getNodes()
        innerDefinitionNodes.size() == 3
        innerDefinitionNodes[0] == new ListNode("mod")
        innerDefinitionNodes[1] == new ListNode("num")
        innerDefinitionNodes[2] == new ListNode("2")

        definitionNodes[2] == new ListNode("0")
    }

    def "quoting example"() {
        given:
        def program = "(filter '(6 4 3 5 2) #'even)"

        when:
        def parsedList = parser.parse(program)

        then:
        def outerListNodes = parsedList.getNodes()
        outerListNodes.size() == 3
        outerListNodes[0] == new ListNode("filter")

        def quotedListParsedList = outerListNodes[1].getParsedList()
        quotedListParsedList.getQuoteType() == QuoteType.LIST
        println(outerListNodes)

        def quotedFunction = outerListNodes[2]
        quotedFunction.getQuoteType() == QuoteType.FUNCTION
        quotedFunction.getValue() == "even"
    }

    def "quoting example with deeper nesting"() {
        given:
        def program = "(non-nil '(a nil (b) (nil) 2))"

        when:
        def parsedList = parser.parse(program)

        then:
        // just enough assertions to check AOK so far
        parsedList.getNodes()[1].getParsedList().getQuoteType() == QuoteType.LIST
        parsedList.getNodes()[1].getParsedList().getNodes()[0].getValue() == "a"
        parsedList.getNodes()[1].getParsedList().getNodes()[1].getValue() == "nil"
        parsedList.getNodes()[1].getParsedList().getNodes()[2].getParsedList().getNodes()[0].getValue() == "b"
    }

    def "function-quoted lambda"() {
        given:
        def program = "(funcall #'(lambda (x) (+ x 1)) 1)"

        when:
        def parsedList = parser.parse(program)

        then:
        // just enough assertions to check AOK so far
        parsedList.getNodes()[0].getValue() == "funcall"
        parsedList.getNodes()[1].getParsedList().getQuoteType() == QuoteType.FUNCTION
        parsedList.getNodes()[1].getParsedList().getNodes()[0].getValue() == "lambda"
        parsedList.getNodes()[1].getParsedList().getNodes()[1].getParsedList().getNodes()[0].getValue() == "x"
        parsedList.getNodes()[1].getParsedList().getNodes()[2].getParsedList().getNodes()[0].getValue() == "+"
        parsedList.getNodes()[1].getParsedList().getNodes()[2].getParsedList().getNodes()[1].getValue() == "x"
        parsedList.getNodes()[1].getParsedList().getNodes()[2].getParsedList().getNodes()[2].getValue() == "1"
        parsedList.getNodes()[2].getValue() == "1"
    }

    def "quoted string example"() {
        given:
        def program = '(setf my-variable (read-from-string "(1 2 3)"))'

        when:
        def parsedList = parser.parse(program)

        then:
        // just enough assertions to check AOK so far
        parsedList.getNodes()[2].getParsedList().getNodes()[1].getValue() == "(1 2 3)"
        parsedList.getNodes()[2].getParsedList().getNodes()[1].getQuoteType() == QuoteType.STRING
    }

    def "type specifier example"() {
        given:
        def program = '(concatenate \'string "Hello, " "world" ". Today is good.")'

        when:
        def parsedList = parser.parse(program)

        then:
        // just enough assertions to check AOK so far
        parsedList.getNodes()[1].getQuoteType() == QuoteType.OTHER
        parsedList.getNodes()[1].getValue() == "string"

    }
}
