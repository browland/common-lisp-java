package evaluator.macro

import evaluator.Evaluator
import evaluator.env.Environment
import reader.NodeBuilder
import spock.lang.Specification
import syntaxtree.Atom
import syntaxtree.RList
import value.Macro

class MacroExpanderSpec extends Specification {
    def "simple quote"() {
        given:
        def evaluator = new Evaluator()
        def macroExpander = new MacroExpander()
        def environment = new Environment()

        def macroDef = """
          (defmacro foo () 
            '(+ 1 2))
        """

        def macro = parseMacro(macroDef)

        def macroInvocation = "(foo)"
        def macroInvocationList = programToRList(macroInvocation)

        when:
        def expandedMacroList = macroExpander.expand(macro, macroInvocationList, evaluator, environment) as RList

        then:
        def firstSymbol = expandedMacroList.nodes().get(0) as Atom
        firstSymbol.value() == "+"

        def firstArg = expandedMacroList.nodes().get(1) as Atom
        firstArg.value() == "1"

        def secondArg = expandedMacroList.nodes().get(2) as Atom
        secondArg.value() == "2"
    }

    def "does not evaluate quoted list operand during macro expansion"() {
        given:
        def evaluator = new Evaluator()
        def macroExpander = new MacroExpander()
        def environment = new Environment()

        def macroDef = """
            (defmacro inspect (x)
                (list 'quote x))
        """

        def macro = parseMacro(macroDef)

        def macroInvocation = "(inspect '(1 2))"
        def macroInvocationList = programToRList(macroInvocation)

        when:
        def expandedMacroList = macroExpander.expand(macro, macroInvocationList, evaluator, environment) as RList

        then:
        def operator = expandedMacroList.nodes().get(0) as Atom
        operator.value() == "quote"

        def quotedArg = expandedMacroList.nodes().get(1) as RList
        def firstListElement = quotedArg.nodes().get(0) as Atom
        firstListElement.value() == "quote"
        def innerList = quotedArg.nodes().get(1) as RList
        def innerListElem1 = innerList.nodes().get(0) as Atom
        innerListElem1.value() == "1"
        def innerListElem2 = innerList.nodes().get(1) as Atom
        innerListElem2.value() == "2"
    }

    def "does not evaluate list invocation operand during macro expansion"() {
        given:
        def evaluator = new Evaluator()
        def macroExpander = new MacroExpander()
        def environment = new Environment()

        def macroDef = """
            (defmacro inspect (x)
                (list 'quote x))
        """

        def macro = parseMacro(macroDef)

        def macroInvocation = "(inspect (list 1 2))"
        def macroInvocationList = programToRList(macroInvocation)

        when:
        def expandedMacroList = macroExpander.expand(macro, macroInvocationList, evaluator, environment) as RList

        then:
        def operator = expandedMacroList.nodes().get(0) as Atom
        operator.value() == "quote"

        def quotedArg = expandedMacroList.nodes().get(1) as RList
        def firstListElement = quotedArg.nodes().get(0) as Atom
        firstListElement.value() == "list"
        def listElem1 = quotedArg.nodes().get(1) as Atom
        listElem1.value() == "1"
        def listElem2 = quotedArg.nodes().get(2) as Atom
        listElem2.value() == "2"
    }

    def "does not evaluate function call invocation operand during macro expansion"() {
        given:
        def evaluator = new Evaluator()
        def macroExpander = new MacroExpander()
        def environment = new Environment()

        def macroDef = """
            (defmacro inspect (x)
                (list 'quote x))
        """

        def macro = parseMacro(macroDef)

        def macroInvocation = "(inspect (+ 1 2))"
        def macroInvocationList = programToRList(macroInvocation)

        when:
        def expandedMacroList = macroExpander.expand(macro, macroInvocationList, evaluator, environment) as RList

        then:
        def operator = expandedMacroList.nodes().get(0) as Atom
        operator.value() == "quote"

        def quotedArg = expandedMacroList.nodes().get(1) as RList
        def firstListElement = quotedArg.nodes().get(0) as Atom
        firstListElement.value() == "+"
        def listElem1 = quotedArg.nodes().get(1) as Atom
        listElem1.value() == "1"
        def listElem2 = quotedArg.nodes().get(2) as Atom
        listElem2.value() == "2"
    }

    def "allows multiple body forms"() {
        given:
        def evaluator = new Evaluator()
        def macroExpander = new MacroExpander()
        def environment = new Environment()

        def macroDef = """
          (defmacro foo () 
            (defvar *x* 1)
            `(+ ,*x* 2))
        """

        def macro = parseMacro(macroDef)

        def macroInvocation = "(foo)"
        def macroInvocationList = programToRList(macroInvocation)

        when:
        def expandedMacroList = macroExpander.expand(macro, macroInvocationList, evaluator, environment)

        then:
        def firstSymbol = expandedMacroList.nodes().get(0) as Atom
        firstSymbol.value() == "+"

        def firstArg = expandedMacroList.nodes().get(1) as Atom
        firstArg.value() == "1"

        def secondArg = expandedMacroList.nodes().get(2) as Atom
        secondArg.value() == "2"
    }

    def parseMacro(macroDef) {
        def macroDefList = programToRList(macroDef)
        def name = macroDefList.nodes().get(1) as Atom
        def bindingsList = macroDefList.nodes().get(2) as RList
        List<Atom> bindings = bindingsList.nodes().collect {it -> (Atom)it}
        def bodyNodes = macroDefList.nodes().subList(3, macroDefList.nodes().size())
        return new Macro(bindings, bodyNodes, name.value())
    }

    def programToRList(program) {
        def listBuilder = new NodeBuilder()
        return listBuilder.build(program).getFirst()
    }
}
