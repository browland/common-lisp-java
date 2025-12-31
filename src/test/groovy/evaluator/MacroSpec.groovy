package evaluator

import evaluator.env.Environment
import evaluator.env.Symbols
import spock.lang.Specification
import value.ConsCellValue
import value.Symbol
import value.Value
import value.ValueType

class MacroSpec extends Specification {
    def "simple test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def macroDef = """
           (defmacro testing (x y)
             `(+ ,x ,y))
        """

        when:
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret("(testing 1 2)")

        then:
        result.getValue() == 3
    }

    def "variadic args test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def macroDef = """
            (defmacro testing (x &rest rest) 
                `(+ ,x (car ',rest)) 
            )
        """

        when:
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret("(testing 1 2 3)")

        then:
        result.getValue() == 3
    }

    def "push macro test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def globalDef = "(defvar *db* nil)"
        def macroDef = """
            (defmacro push (item place)
                `(setf ,place (cons ,item ,place)))
        """
        def push = "(push 1 *db*)"

        when:
        interpreter.interpret(globalDef)
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret(push)

        then:
        result.getType() == ValueType.CONS_CELL

        Symbol dbSymbol = new Symbols().internSymbol("*db*");
        ConsCellValue updatedConsValue = env.get(dbSymbol).get() as ConsCellValue
        updatedConsValue.value.car().value == 1
    }
}
