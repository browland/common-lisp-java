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

    def "block comment, single line"() {
        given:
        def program = "#| testing testing |#"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.isEmpty()
    }

    def "block comment, two calls over two lines"() {
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

    def "block comment, two calls over two lines, followed by char literal"() {
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

    def "nested block comment"() {
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
        def program = "1 3.14159s0 3.14159d0"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 3
        result.get(0) == "1"
        result.get(1) == "3.14159s0"
        result.get(2) == "3.14159d0"
    }

    def "string and double-quote escaping"() {
        given:
        def program = "\"hello\\\" \\\"world\""
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "\"hello\\\" \\\"world\""
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

    def "binary value"() {
        given:
        def program = "#b0010"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "#b0010"
    }

    def "octal value"() {
        given:
        def program = "#o111"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "#o111"
    }

    def "hex value"() {
        given:
        def program = "#xa0d"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "#xa0d"
    }

    def "ratio value"() {
        given:
        def program = "1/2"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "1/2"
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

    def "vector value"() {
        given:
        def program = "#(1 2 3)"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 1
        result.get(0) == "#(1 2 3)"
    }

    def "quoted value"() {
        given:
        def program = "'a"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 2
        result.get(0) == "'"
        result.get(1) == "a"
    }

    def "function quote usage"() {
        given:
        def program = "(mapcar #'foo '(1 2 3))"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 11
        result.get(0) == "("
        result.get(1) == "mapcar"
        result.get(2) == "#'"
        result.get(3) == "foo"
        result.get(4) == "'"
        result.get(5) == "("
        result.get(6) == "1"
        result.get(7) == "2"
        result.get(8) == "3"
        result.get(9) == ")"
        result.get(10) == ")"
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

    def "quoted list"() {
        given:
        def program = "'(1 2 3)"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 6
        result.get(0) == "'"
        result.get(1) == "("
        result.get(2) == "1"
        result.get(3) == "2"
        result.get(4) == "3"
        result.get(5) == ")"
    }

    def "quasi-quoted list"() {
        given:
        def program = "`(1 2 3)"
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 6
        result.get(0) == "`"
        result.get(1) == "("
        result.get(2) == "1"
        result.get(3) == "2"
        result.get(4) == "3"
        result.get(5) == ")"
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

    def "preceding whitespace"() {
        given:
        def program = """
        (+ 1 2)
        """
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 5
        result.get(0) == "("
    }

    def "unquote symbol"() {
        given:
        def program = """
        ,x
        """
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 2
        result.get(0) == ","
        result.get(1) == "x"
    }

    def "unquote list"() {
        given:
        def program = """
        ,(x y z)
        """
        SimplerTokeniser tokeniser = new SimplerTokeniser()

        when:
        def result = tokeniser.tokenise(program)

        then:
        result.size() == 6
        result.get(0) == ","
        result.get(1) == "("
        result.get(2) == "x"
        result.get(3) == "y"
        result.get(4) == "z"
        result.get(5) == ")"
    }
}
