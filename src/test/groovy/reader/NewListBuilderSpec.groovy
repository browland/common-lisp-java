package reader

import spock.lang.Specification
import syntaxtree.Atom
import syntaxtree.RList

class NewListBuilderSpec extends Specification {
    def "simple list"() {
        given:
        var builder = new NewListBuilder()
        var program = """(+ 1 2)"""

        when:
        var list = builder.build(program) as RList

        then:
        list.size() == 3
        var atom1 = list.get(0) as Atom
        atom1.value() == "+"
        var atom2 = list.get(1) as Atom
        atom2.value() == "1"
        var atom3 = list.get(2) as Atom
        atom3.value() == "2"
    }

    def "cons pair (improper list)"() {
        given:
        var builder = new NewListBuilder()
        var program = """(1 . 2)"""

        when:
        var list = builder.build(program) as RList

        then:
        list.size() == 3
        var atom1 = list.get(0) as Atom
        atom1.value() == "cons"
        var atom2 = list.get(1) as Atom
        atom2.value() == "1"
        var atom3 = list.get(2) as Atom
        atom3.value() == "2"
    }

    def "quoted atom"() {
        given:
        var builder = new NewListBuilder()
        var program = "'foo"

        when:
        var list = builder.build(program) as RList

        then:
        list.size() == 2
        var atom1 = list.get(0) as Atom
        atom1.value() == "quote"
        var atom2 = list.get(1) as Atom
        atom2.value() == "foo"
    }

    def "quoted list"() {
        given:
        var builder = new NewListBuilder()
        var program = "'(1 2)"

        when:
        var list = builder.build(program) as RList

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
}
