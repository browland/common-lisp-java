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

    // todo this breaks because there's an outer quote in the body.  But handling it where we
    //      handle quasiquote breaks the variadic args test, as the quote in that one needs to
    //      stay until evaluation time.  Naively stripping quotes at all levels is at breaking point.
    //      The solution might just be to strip *outer* list/quote/quasiquote only?
    //      Issue is that technically we're supposed to evalute the macro body *once* at expansion time
    //      in order to get the code to evaluate.  But this will result in a value which I can't get
    //      back to an RList (unless I add support for that).  So I'm stuck with naively stripping outer
    //      quote/list/quasiquote and unquoting naively in the macro expander for now.
    def "regular quote test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def macroDef = """
          (defmacro foo () 
            '(+ 1 2))
        """

        when:
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret("(foo)")

        then:
        result.getValue() == 3
    }
}
