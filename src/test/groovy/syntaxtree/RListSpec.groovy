package syntaxtree

import spock.lang.Specification

class RListSpec extends Specification {
    def "toString() for single-depth list"() {
        given:
        def rList = new RList.Builder()
                .addNodeBuilder(new Atom.Builder().value("+"))
                .addNodeBuilder(new Atom.Builder().value("1"))
                .addNodeBuilder(new Atom.Builder().value("2"))
                .build()
        when:
        def rListString = rList.toString()

        then:
        rListString == "(+ 1 2)"
    }

    def "toString() for nested list"() {
        given:
        def nestedRListBuilder = new RList.Builder()
                .addNodeBuilder(new Atom.Builder().value("+"))
                .addNodeBuilder(new Atom.Builder().value("1"))
                .addNodeBuilder(new Atom.Builder().value("2"));

        def rList = new RList.Builder()
                .addNodeBuilder(new Atom.Builder().value("+"))
                .addNodeBuilder(new Atom.Builder().value("1"))
                .addNodeBuilder(nestedRListBuilder)
                .build()
        when:
        def rListString = rList.toString()

        then:
        rListString == "(+ 1 (+ 1 2))"
    }
}
