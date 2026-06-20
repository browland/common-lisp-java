package syntaxtree

import spock.lang.Specification

class RListSpec extends Specification {
    def "toString() for single-depth list"() {
        given:
        def rList = new RList(false, List.of(new Atom("+"), new Atom("1"), new Atom("2")), false)

        when:
        def rListString = rList.toString()

        then:
        rListString == "(+ 1 2)"
    }

    def "toString() for nested list"() {
        given:
        def nestedRList = new RList(false, List.of(new Atom("+"), new Atom("1"), new Atom("2")), false)
        def rList = new RList(false, List.of(new Atom("+"), new Atom("1"), nestedRList), false)

        when:
        def rListString = rList.toString()

        then:
        rListString == "(+ 1 (+ 1 2))"
    }
}
