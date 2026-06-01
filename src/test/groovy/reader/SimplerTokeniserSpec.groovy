package reader

import spock.lang.Specification

class SimplerTokeniserSpec extends Specification {

    def "character literals"() {
        given:
        def program = "#\\space #\\x"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 2
        result.get(0) == "#\\space"
        result.get(1) == "#\\x"
    }

    def "block quote, single line"() {
        given:
        def program = "#| testing testing |#"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.isEmpty()
    }

    def "block quote, two calls over two lines"() {
        given:
        def program1 = "#| testing "
        def program2 = "  testing |#"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result1 = tokeniser.tokenise(program1)
        def result2 = tokeniser.tokenise(program2)

        then:
        result1.isEmpty()
        result2.isEmpty()
    }

    def "block quote, two calls over two lines, followed by char literal"() {
        given:
        def program1 = "#| testing "
        def program2 = "  testing |# #\\x"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result1 = tokeniser.tokenise(program1)
        def result2 = tokeniser.tokenise(program2)

        then:
        result1.isEmpty()
        result2.isEmpty()
    }

    def "nested block quote"() {
        given:
        def program = """#| This is a block comment which
                   can span multiple lines and
                    #|
                       they can be nested!
                    |#
                |#"""
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.isEmpty()
    }

    def "open close list"() {
        given:
        def program = "(())"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 4
        result.get(0) == "("
        result.get(1) == "("
        result.get(2) == ")"
        result.get(3) == ")"
    }

    def "numeric types"() {
        given:
        def program = "1 1.23"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 2
        result.get(0) == "1"
        result.get(1) == "1.23"
    }

    def "string types"() {
        given:
        def program = "\"hello\" \"world\""
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 2
        result.get(0) == "\"hello\""
        result.get(1) == "\"world\""
    }

    def "complex number"() {
        given:
        def program = "#C(1 2)"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "#C(1 2)"
    }

    def "simple atom"() {
        given:
        def program = "hello"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "hello"
    }

    def "simple list"() {
        given:
        def program = "(1 2 3)"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 5
        result.get(0) == "("
        result.get(1) == "1"
        result.get(2) == "2"
        result.get(3) == "3"
        result.get(4) == ")"
    }

    def "more complex test"() {
        given:
        def program = "(1 (2 3) #\\a #C(1 2) ())"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 11
        result.get(0) == "("
        result.get(1) == "1"
        result.get(2) == "("
        result.get(3) == "2"
        result.get(4) == "3"
        result.get(5) == ")"
        result.get(6) == "#\\a"
        result.get(7) == "#C(1 2)"
        result.get(8) == "("
        result.get(9) == ")"
        result.get(10) == ")"
    }
}
