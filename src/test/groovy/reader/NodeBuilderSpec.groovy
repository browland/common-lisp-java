package reader

import spock.lang.Specification
import syntaxtree.Atom
import syntaxtree.RList

class NodeBuilderSpec extends Specification {
    def "simple list"() {
        given:
        var builder = new NodeBuilder()
        var program = """(+ 1 2)"""

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
        var atom1 = list.get(0) as Atom
        atom1.value() == "+"
        var atom2 = list.get(1) as Atom
        atom2.value() == "1"
        var atom3 = list.get(2) as Atom
        atom3.value() == "2"
    }

    def "apply form with no space before open paren"() {
        given:
        var builder = new NodeBuilder()
        var program = """'(1 2 3(3 2 1))"""

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        list.get(1).size() == 4
        list.get(1).get(3).size() == 3
    }

    def "cons pair (improper list)"() {
        given:
        var builder = new NodeBuilder()
        var program = """(1 . 2)"""

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "1"
        var atom2 = list.get(1) as Atom
        atom2.value() == "2"
    }

    def "quoted atom"() {
        given:
        var builder = new NodeBuilder()
        var program = "'foo"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "quote"
        var atom2 = list.get(1) as Atom
        atom2.value() == "foo"
    }

    def "quoted function"() {
        given:
        var builder = new NodeBuilder()
        var program = "#'foo"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "function"
        var atom2 = list.get(1) as Atom
        atom2.value() == "foo"
    }

    def "quoted list"() {
        given:
        var builder = new NodeBuilder()
        var program = "'(1 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "quote"
        var innerList = list.get(1) as RList
        innerList.size() == 2
        def innerAtom1 = innerList.get(0) as Atom
        innerAtom1.value() == "1"
        def innerAtom2 = innerList.get(1) as Atom
        innerAtom2.value() == "2"
    }

    def "quasiquoted list"() {
        given:
        var builder = new NodeBuilder()
        var program = "`(1 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "quasiquote"
        var innerList = list.get(1) as RList
        innerList.size() == 2
        def innerAtom1 = innerList.get(0) as Atom
        innerAtom1.value() == "1"
        def innerAtom2 = innerList.get(1) as Atom
        innerAtom2.value() == "2"
    }

    def "unquoted list"() {
        given:
        var builder = new NodeBuilder()
        var program = ",(1 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "unquote"
        var innerList = list.get(1) as RList
        innerList.size() == 2
        def innerAtom1 = innerList.get(0) as Atom
        innerAtom1.value() == "1"
        def innerAtom2 = innerList.get(1) as Atom
        innerAtom2.value() == "2"
    }

    def "unquote splicing list"() {
        given:
        var builder = new NodeBuilder()
        var program = ",@(1 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "unquote-splicing"
        var innerList = list.get(1) as RList
        innerList.size() == 2
        def innerAtom1 = innerList.get(0) as Atom
        innerAtom1.value() == "1"
        def innerAtom2 = innerList.get(1) as Atom
        innerAtom2.value() == "2"
    }

    def "test breakage due to explicit use of function which we might accidentally auto close"() {
        given:
        var builder = new NodeBuilder()
        var program = "(defvar x (function add))"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
        list.get(0) == new Atom("defvar")
        list.get(1) == new Atom("x")
        def functionQuoteList = list.get(2) as RList
        functionQuoteList.size() == 2
        functionQuoteList.get(0) == new Atom("function")
        functionQuoteList.get(1) == new Atom("add")
    }

    def "test breakage due to with null returned"() {
        given:
        var builder = new NodeBuilder()
        var program = "(defvar z #'add)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
        list.get(0) == new Atom("defvar")
        list.get(1) == new Atom("z")
        def functionQuoteList = list.get(2) as RList
        functionQuoteList.size() == 2
        functionQuoteList.get(0) == new Atom("function")
        functionQuoteList.get(1) == new Atom("add")
    }

    def "multiple nested list"() {
        given:
        var builder = new NodeBuilder()
        var program = "(add (add 1 2) 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
        list.get(0) == new Atom("add")
        list.get(2) == new Atom("2")

        var innerList = list.get(1) as RList
        innerList.get(0) == new Atom("add")
        innerList.get(1) == new Atom("1")
        innerList.get(2) == new Atom("2")
    }

    def "quote list and function quote"() {
        given:
        var builder = new NodeBuilder()
        var program = "(filter '(6 4 3 5 2) #'even)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
        list.get(0) == new Atom("filter")

        var quoteList = list.get(1) as RList
        quoteList.get(0) == new Atom("quote")

        var numsList = quoteList.get(1) as RList
        numsList.get(0) == new Atom("6")
        numsList.get(1) == new Atom("4")
        numsList.get(2) == new Atom("3")
        numsList.get(3) == new Atom("5")
        numsList.get(4) == new Atom("2")

        var functionQuoteList = list.get(2) as RList
        functionQuoteList.size() == 2
        functionQuoteList.get(0) == new Atom("function")
        functionQuoteList.get(1) == new Atom("even")
    }

    def "string literal"() {
        given:
        var builder = new NodeBuilder()
        var program = "(format t \"hello world\")"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
        list.get(0) == new Atom("format")
        list.get(1) == new Atom("t")
        list.get(2) == new Atom("\"hello world\"")
    }

    def "keyword symbols and list function"() {
        given:
        var builder = new NodeBuilder()
        var program = "(list :a 1 :b 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 5
        list.get(0) == new Atom("list")
        list.get(1) == new Atom(":a")
        list.get(2) == new Atom("1")
        list.get(3) == new Atom(":b")
        list.get(4) == new Atom("2")
    }

    def "multiple string literals"() {
        given:
        var builder = new NodeBuilder()
        var program = "(make-cd \"Roses\" \"Kathy Mattea\")"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
        list.get(0) == new Atom("make-cd")
        list.get(1) == new Atom("\"Roses\"")
        list.get(2) == new Atom("\"Kathy Mattea\"")
    }

    def "complex lambda"() {
        given:
        var builder = new NodeBuilder()
        var program = "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"

        when:
        var outerLambdaApply = builder.build(program)[0] as RList

        then:
        // Outer lambda application ... We expect 2 parts: inner lambda application, and an argument to this lambda application
        outerLambdaApply.size() == 2
        outerLambdaApply.get(1) == new Atom("5")

        // Inner lambda application ... We expect 2 parts: outer lambda body, and an argument to this lambda application
        var innerLambdaApply = outerLambdaApply.get(0) as RList
        innerLambdaApply.size() == 2
        innerLambdaApply.get(1) == new Atom("10")

        // Outer lambda itself ... We expect 3 parts: "lambda", the bindings list containing just x, and the body which is just the inner lambda
        var outerLambda = innerLambdaApply.get(0) as RList
        outerLambda.size() == 3
        outerLambda.get(0) == new Atom("lambda")
        def outerLambdaBindings = outerLambda.get(1) as RList
        outerLambdaBindings.get(0) == new Atom("x")

        // Inner lambda itself ... We expect 3 parts: "lambda", the bindings list containing just y, and the body which is a list of 3 atoms
        var innerLambda = outerLambda.get(2) as RList
        innerLambda.size() == 3
        innerLambda.get(0) == new Atom("lambda")
        var innerLambdaBindings = innerLambda.get(1) as RList
        innerLambdaBindings.get(0) == new Atom("y")

        var innerLambdaBody = innerLambda.get(2) as RList
        innerLambdaBody.size() == 3
        innerLambdaBody.get(0) == new Atom("+")
        innerLambdaBody.get(1) == new Atom("x")
        innerLambdaBody.get(2) == new Atom("y")
    }

    def "quote list - should not evaluate as form"() {
        given:
        var builder = new NodeBuilder()
        var program = "'(add 1 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        list.get(0) == new Atom("quote")

        var innerList = list.get(1) as RList
        innerList.size() == 3
        innerList.get(0) == new Atom("add")
        innerList.get(1) == new Atom("1")
        innerList.get(2) == new Atom("2")
    }

    def "quasiquote with unquote symbol"() {
        given:
        var builder = new NodeBuilder()
        var program = "`(add ,x 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        list.get(0) == new Atom("quasiquote")

        var innerList = list.get(1) as RList
        innerList.size() == 3
        innerList.get(0) == new Atom("add")

        var unquoteSplicingList = innerList.get(1) as RList
        unquoteSplicingList.get(0) == new Atom("unquote")
        unquoteSplicingList.get(1) == new Atom("x")

        innerList.get(2) == new Atom("2")
    }

    def "quasiquote with unquote splicing"() {
        given:
        var builder = new NodeBuilder()
        var program = "`(add ,@x 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        list.get(0) == new Atom("quasiquote")

        var innerList = list.get(1) as RList
        innerList.size() == 3
        innerList.get(0) == new Atom("add")

        var unquoteSplicingList = innerList.get(1) as RList
        unquoteSplicingList.get(0) == new Atom("unquote-splicing")
        unquoteSplicingList.get(1) == new Atom("x")

        innerList.get(2) == new Atom("2")
    }

    def "simple atom"() {
        given:
        var builder = new NodeBuilder()
        var program = "1"

        when:
        var atom = builder.build(program)[0] as Atom

        then:
        atom.value() == "1"
    }

    def "improper list"() {
        given:
        var builder = new NodeBuilder()
        var program = "(1 . 2)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 2
        list.get(0) == new Atom("1")
        list.get(1) == new Atom("2")
    }

    def "string literal containing spaces"() {
        given:
        var builder = new NodeBuilder()
        var program = "\"A steel door blocks your way. Key? (yes/no)\""

        when:
        var atom = builder.build(program)[0] as Atom

        then:
        atom.value() == "\"A steel door blocks your way. Key? (yes/no)\""
    }
}
