package reader

import spock.lang.Specification

class CharacterReaderSpec extends Specification {

    def "reads simple program"() {
        given:
        def reader = new Reader()
        def characterReader = new CharacterReader(reader)
        def program = "(add (add 1 2) 2)"

        when:
        characterReader.read(program)
        def outerList = reader.getResult()

        then:
        outerList.depth() == 0

        def nodes = outerList.nodes()
        nodes.size() == 3
        nodes.get(0) instanceof Atom
        ((Atom)nodes.get(0)).value() == "add"
        ((Atom)nodes.get(2)).value() == "2"

        def innerResult = nodes.get(1)
        innerResult instanceof RList

        def innerList = (RList)innerResult
        innerList.depth() == 1

        def innerNodes = innerList.nodes()
        innerNodes.size() == 3
        ((Atom)innerNodes.get(0)).value() == "add"
        ((Atom)innerNodes.get(1)).value() == "1"
        ((Atom)innerNodes.get(2)).value() == "2"
    }

    def "reads program with quoted list and quoted function"() {
        def reader = new Reader()
        def characterReader = new CharacterReader(reader)
        def program = "(filter '(6 4 3 5 2) #'even)"

        when:
        characterReader.read(program)
        def outerList = reader.getResult()

        then:
        def outerNodes = outerList.nodes()
        ((Atom)outerNodes.get(0)).value() == "filter"

        def innerResult = outerNodes.get(1)
        innerResult instanceof RList
        def innerList = (RList)innerResult
        innerList.prefix() == "'"

        def innerNodes = innerList.nodes()
        ((Atom)innerNodes[0]).value() == "6"
        ((Atom)innerNodes[1]).value() == "4"
        ((Atom)innerNodes[2]).value() == "3"
        ((Atom)innerNodes[3]).value() == "5"
        ((Atom)innerNodes[4]).value() == "2"

        def atom = (Atom) outerNodes.get(2)
        atom.value() == "even"
        atom.prefix() == "#'"
    }
}
