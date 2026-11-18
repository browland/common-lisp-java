package compiler.treewalker

import spock.lang.Specification
import syntaxtree.Atom

class TypeCoercerSpec extends Specification {

    def "detects integer typed atom"() {
        given:
        Atom atom = new Atom("1")

        when:
        TypedAtom<?> typedAtom = TypeCoercer.coerceType(atom)

        then:
        typedAtom instanceof IntAtom
    }

    def "detects string typed atom"() {
        given:
        Atom atom = new Atom("\"hello\"")

        when:
        TypedAtom<?> typedAtom = TypeCoercer.coerceType(atom)

        then:
        typedAtom instanceof StringAtom
    }

    def "detects symbol typed atom"() {
        given:
        Atom atom = new Atom("hello")

        when:
        TypedAtom<?> typedAtom = TypeCoercer.coerceType(atom)

        then:
        typedAtom instanceof SymbolAtom
    }

}
