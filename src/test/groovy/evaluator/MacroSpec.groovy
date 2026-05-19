package evaluator

import evaluator.env.Environment
import evaluator.env.Symbols
import spock.lang.Specification
import value.ConsCellValue
import value.Symbol
import value.SymbolValue
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
                `(setq ,place (cons ,item ,place)))
        """
        def push = "(push 1 *db*)"

        when:
        interpreter.interpret(globalDef)
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret(push)

        then:
        result.getType() == ValueType.CONS_CELL

        Symbol dbSymbol = new Symbols().internSymbol("*db*");
        ConsCellValue updatedConsValue = env.getVariable(dbSymbol).get() as ConsCellValue
        updatedConsValue.value.car().value == 1
    }

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

    def "macro expansion does not have side effects on global variable"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        // We need to use progn as we don't have a way to interpret multiple forms programmatically yet
        def setupProgram = """
            (progn
                (defvar *x* 0)
    
                (defun bump ()
                    (setq *x* (+ *x* 1))
                    123)
    
                (defmacro noop (x)
                    `(list 1 2))
                    
                (noop (bump))  
            )
        """

        when:
        interpreter.interpret(setupProgram)
        Value<?> result = interpreter.interpret("(+ *x* 1)")

        then:
        result.getValue() == 1
    }

    def "macro can inspect syntax shape - false"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def setupProgram = """
            (defmacro is-list-call (x)
                (if (and (consp x)
                    (eq (car x) 'list))
                    t
                    nil))
        """

        when:
        interpreter.interpret(setupProgram)
        Value<?> result = interpreter.interpret("(is-list-call '(1 2))")

        then:
        result.getValue() instanceof Symbol
        def resultValue = result.getValue() as Symbol
        resultValue.name() == "nil"
    }

    def "macro can inspect syntax shape - true"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def setupProgram = """
            (defmacro is-list-call (x)
                (if (and (consp x)
                    (eq (car x) 'list))
                    t
                    nil))
        """

        when:
        interpreter.interpret(setupProgram)
        Value<?> result = interpreter.interpret("(is-list-call (list 1 2))")

        then:
        result.getValue() instanceof Symbol
        def resultValue = result.getValue() as Symbol
        resultValue.name() == "t"
    }

    def "macro expansion does not evaluate quoted list operand"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        // We need to use progn as we don't have a way to interpret multiple forms programmatically yet
        def program = """
            (progn
                (defmacro inspect (x)
                    (list 'quote x))
                    
                (let ((y 42))
                    (inspect y))
            )
        """

        when:
        Value<?> result = interpreter.interpret(program)

        then:
        result instanceof SymbolValue
        def symbolResult = result as SymbolValue
        def symbol = symbolResult.value as Symbol
        symbol.name() == "y"
    }

    def "destructuring test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def macroDef = """
           (defmacro testing ((x y) z)
             `(+ ,x ,y))
        """

        when:
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret("(testing '(1 2) 3)")

        then:
        result.getValue() == 3
    }
}
