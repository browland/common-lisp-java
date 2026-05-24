package value

import evaluator.env.Symbols
import reader.CharacterReader
import spock.lang.Specification
import syntaxtree.ParseElementBuilder
import syntaxtree.SyntaxTreeBuilder

class ConsCellValueSpec extends Specification {

    def "test construction from syntax tree"() {
        given:
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        def program = "(add (add 1 2) 2)"

        when:
        characterReader.read(program)
        def outerList = syntaxTreeBuilder.getResult()
        def result = ConsCellValue.fromRList(outerList)

        then:
        def firstCons = result.value as ConsCell
        def firstElement = firstCons.car() as SymbolValue
        firstElement.getValue() == Symbols.internSymbol("add")

        def secondCons = firstCons.cdr().getValue() as ConsCell
        def secondElement = secondCons.car() as ConsCellValue // Second element is itself a list

        def firstInnerListCons = secondElement.getValue()
        def firstInnerListSymbolValue = firstInnerListCons.car() as SymbolValue
        firstInnerListSymbolValue.getValue() == Symbols.internSymbol("add")

        def secondInnerListElement = firstInnerListCons.cdr() as ConsCellValue
        def secondInnerListCons = secondInnerListElement.getValue() as ConsCell
        def secondInnerListValue = secondInnerListCons.car() as IntegerValue
        secondInnerListValue.getValue() == 1

        def thirdInnerListElement = secondInnerListCons.cdr() as ConsCellValue
        def thirdInnerListCons = thirdInnerListElement.getValue() as ConsCell
        def thirdInnerListValue = thirdInnerListCons.car() as IntegerValue
        thirdInnerListValue.getValue() == 2

        def thirdCons = secondCons.cdr().getValue() as ConsCell
        def thirdOuterListValue = thirdCons.car() as IntegerValue // Second element is itself a list
        thirdOuterListValue.getValue() == 2
    }

    def "to string with single depth list"() {
        given:
        def atom1 = new SymbolValue(Symbols.internSymbol("+"))
        def atom2 = new IntegerValue(1)
        def atom3 = new IntegerValue(2)

        def cons = ConsCell.fromValue(atom3)
            .push(atom2)
            .push(atom1)

        when:
        def consString = cons.toString()

        then:
        consString == "(+ 1 2)"
    }
}
