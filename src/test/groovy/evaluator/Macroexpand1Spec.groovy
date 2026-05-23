package evaluator

import evaluator.env.Environment
import spock.lang.Specification
import value.Value

class Macroexpand1Spec extends Specification {

    def "basic test"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        // This takes some explanation.  Think about what you want the result of expansion to be.  We want this macro
        // to expand to something like:
        // (car '(1 2))
        // So we need to:
        // 1. quote the (car ' bit as that's literally all we want for the first bit.  And we need to quasiquote so we can unquote in a bit
        // 2. unquote the x so we get the bound value
        // 3. quote the bound list as that's the only way we can refer to the literal list of data i.e. '(1 2)
        // So again, think about what code you want expanded, and just work back
        def macroDef = """
           (defmacro testing (x)
             `(car ',x))
        """

        when:
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret("(macroexpand-1 '(testing (1 2)))")

        then:
        // expect:
        def resultString = result.toString()
        resultString == "(car (quote (1 2)))"
    }

    def "expansion to simple atom"() {
        given:
        def env = new Environment()
        def interpreter = new Interpreter(env)

        def macroDef = """
           (defmacro testing (x)
             `1
           )
        """

        when:
        interpreter.interpret(macroDef)
        Value<?> result = interpreter.interpret("(macroexpand-1 '(testing (1 2)))")

        then:
        // expect:
        def resultString = result.toString()
        resultString == "1"
    }
}
