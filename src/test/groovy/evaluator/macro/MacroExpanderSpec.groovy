package evaluator.macro

import reader.CharacterReader
import spock.lang.Specification
import syntaxtree.Atom
import syntaxtree.ParseElementBuilder
import syntaxtree.RList
import syntaxtree.SyntaxTreeBuilder
import value.Macro

class MacroExpanderSpec extends Specification {
    def "simple quote"() {
        given:
        def macroExpander = new MacroExpander()

        def macroDef = """
          (defmacro foo () 
            '(+ 1 2))
        """

        // Parse to Macro
        def macroDefList = programToRList(macroDef)
        def bindingsList = macroDefList.nodes().get(2) as RList
        List<Atom> bindings = bindingsList.nodes().collect {it -> (Atom)it}
        def macroBody = macroDefList.nodes().get(3) as RList
        def macro = new Macro(bindings, macroBody)

        def macroInvocation = "(foo)"
        def macroInvocationList = programToRList(macroInvocation)

        when:
        def expandedMacroList = macroExpander.expand(macro, macroInvocationList) as RList

        then:
        def firstSymbol = expandedMacroList.nodes().get(0) as Atom
        firstSymbol.value() == "+"
    }

    def programToRList(program) {
        def syntaxTreeBuilder = new SyntaxTreeBuilder()
        def reader = new ParseElementBuilder(syntaxTreeBuilder)
        def characterReader = new CharacterReader(reader)
        characterReader.read(program)
        return syntaxTreeBuilder.getResult()
    }
}
