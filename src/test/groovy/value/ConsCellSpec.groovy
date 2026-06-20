package value

import evaluator.env.Symbols
import spock.lang.Specification

class ConsCellSpec extends Specification {
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

    def "improper list reverse works"() {
        given:
        def atom1 = new IntegerValue(1)
        def atom2 = new IntegerValue(2)

        def cons = new ConsCell(atom1, atom2)

        when:
        def reversedCons = ConsCell.reverse(cons)

        then:
        reversedCons.toString() == "(2 . 1)"
    }
}
