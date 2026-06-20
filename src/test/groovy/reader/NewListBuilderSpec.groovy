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
        var builder = new NewListBuilder()
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
        var builder = new NewListBuilder()
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
        var builder = new NewListBuilder()
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
        var builder = new NewListBuilder()
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
        var builder = new NewListBuilder()
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
        var builder = new NewListBuilder()
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
        var builder = new NewListBuilder()
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
        var builder = new NewListBuilder()
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

    // we auto-close the function list too early as we thought we'd created the list ourselves via function quote :-/
    def "test breakage due to explicit use of function which we might accidentally auto close"() {
        given:
        var builder = new NewListBuilder()
        var program = "(defvar x (function add))"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
    }

    def "test breakage due to with null returned"() {
        given:
        var builder = new NewListBuilder()
        var program = "(defvar z #'add)"

        when:
        var list = builder.build(program)[0] as RList

        then:
        list.size() == 3
    }

    // todo tests ported over from old reader tests
//    def program = "(add (add 1 2) 2)"
//    def program = "(filter '(6 4 3 5 2) #'even)"
//    def program = "(format t \"hello world\")"
//    def program = "(list :a 1 :b 2)"
//    def program = "(make-cd \"Roses\" \"Kathy Mattea\")"
//    def program = "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)"
//    def program = "'(add 1 2)"
//    def program = "`(add ,x 2)"
//    def program = "`(add ,@x 2)"
//    def program = "1"
//    def program = "(1 . 2)"
//    def program = "\"A steel door blocks your way. Key? (yes/no)\""
}
