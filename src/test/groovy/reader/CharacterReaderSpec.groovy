package reader

import spock.lang.Specification
import syntaxtree.Atom
import syntaxtree.QuoteType
import syntaxtree.RList
import syntaxtree.ParseElementBuilder
import syntaxtree.SyntaxTreeBuilder

class CharacterReaderSpec extends Specification {

    def "reads simple program"() {
        given:
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "(add (add 1 2) 2)"

        when:
        characterReader.read(program)
        def outerList = syntaxTreeBuilder.getResult()

        then:
        outerList.depth() == 0

        outerList.size() == 3
        outerList.get(0) instanceof Atom
        ((Atom)outerList.get(0)).value() == "add"
        ((Atom)outerList.get(2)).value() == "2"

        def innerList = (RList)outerList.get(1)
        innerList.depth() == 1

        innerList.size() == 3
        ((Atom)innerList.get(0)).value() == "add"
        ((Atom)innerList.get(1)).value() == "1"
        ((Atom)innerList.get(2)).value() == "2"
    }

    def "reads program with quoted list and quoted function"() {
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "(filter '(6 4 3 5 2) #'even)"

        when:
        characterReader.read(program)
        def outerList = syntaxTreeBuilder.getResult()

        then:
        ((Atom)outerList.get(0)).value() == "filter"

        def innerList = (RList)outerList.get(1)
        innerList.prefix() == "'"

        ((Atom)innerList.get(0)).value() == "6"
        ((Atom)innerList.get(1)).value() == "4"
        ((Atom)innerList.get(2)).value() == "3"
        ((Atom)innerList.get(3)).value() == "5"
        ((Atom)innerList.get(4)).value() == "2"

        def atom = (Atom) outerList.get(2)
        atom.value() == "even"
        atom.prefix() == "#'"
    }

    def "reads string with space within"() {
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "(format t \"hello world\")"

        when:
        characterReader.read(program)
        def outerList = syntaxTreeBuilder.getResult()

        then:
        ((Atom)outerList.get(2)).value() == "hello world"
        ((Atom)outerList.get(2)).prefix() == "\""
        ((Atom)outerList.get(2)).suffix() == "\""
        ((Atom)outerList.get(2)).quoteType() == QuoteType.STRING
    }

    def "reads keyword symbol"() {
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "(list :a 1 :b 2)"

        when:
        characterReader.read(program)
        def outerList = syntaxTreeBuilder.getResult()

        then:
        ((Atom)outerList.get(1)).value() == "a"
        ((Atom)outerList.get(1)).prefix() == ":"
        ((Atom)outerList.get(1)).quoteType() == QuoteType.KEYWORD
    }

    def "reads string issue"() {
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "(make-cd \"Roses\" \"Kathy Mattea\")"

        when:
        characterReader.read(program)
        def outerList = syntaxTreeBuilder.getResult()

        then:
        ((Atom)outerList.get(1)).value() == "Roses"
        ((Atom)outerList.get(1)).quoteType() == QuoteType.STRING
    }

    def "reads complex lambda"() {
        given:
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"

        when:
        characterReader.read(program)
        def outerLambdaApplicationList = syntaxTreeBuilder.getResult()

        then:
        outerLambdaApplicationList.depth() == 0

        // (( (lambda (x) (lambda (y) (+ x y))) 10) 5)
        outerLambdaApplicationList.size() == 2
        outerLambdaApplicationList.get(0) instanceof RList
        outerLambdaApplicationList.get(1) instanceof Atom
        ((Atom)outerLambdaApplicationList.get(1)).value() == "5"

        // ( (lambda (x) (lambda (y) (+ x y))) 10)
        def innerLambdaApplicationList = (RList)outerLambdaApplicationList.get(0)
        innerLambdaApplicationList.depth() == 1

        innerLambdaApplicationList.size() == 2
        innerLambdaApplicationList.get(0) instanceof RList
        innerLambdaApplicationList.get(1) instanceof Atom

        // (lambda (x) (lambda (y) (+ x y)))
        def outerLambdaDefinitionList = (RList)innerLambdaApplicationList.get(0)
        outerLambdaDefinitionList.size() == 3
        ((Atom)outerLambdaDefinitionList.get(0)).value() == "lambda"
        ((Atom)((RList)outerLambdaDefinitionList.get(1)).nodes()[0]).value() == "x"

        // (lambda (y) (+ x y))
        def innerLambdaDefinitionList = (RList)(outerLambdaDefinitionList.nodes()[2])
        innerLambdaDefinitionList.size() == 3
        ((Atom)innerLambdaDefinitionList.get(0)).value() == "lambda"
        ((Atom)((RList)innerLambdaDefinitionList.get(1)).nodes()[0]).value() == "y"
    }

    def "reads quoted list"() {
        given:
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "'(add 1 2)"

        when:
        characterReader.read(program)
        def outerList = syntaxTreeBuilder.getResult()

        then:
        // result should be: (quote (add 1 2))
        outerList.depth() == 0

        outerList.size() == 2
        outerList.get(0) instanceof Atom
        ((Atom)outerList.get(0)).value() == "quote"

        def innerList = (RList)outerList.get(1)
        innerList.depth() == 1

        innerList.size() == 3
        ((Atom)innerList.get(0)).value() == "add"
        ((Atom)innerList.get(1)).value() == "1"
        ((Atom)innerList.get(2)).value() == "2"
    }
}
