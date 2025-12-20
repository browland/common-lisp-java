package evaluator;

import spock.lang.Specification;
import value.Value;

class LambdaSpec extends Specification {

    def "simple test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def program = """
            (( (lambda (x) (lambda (y) (+ x y))) 10) 5)
        """

        when:
        Value<?> result = evaluator.evaluate(program, env)

        then:
        result.value() == 15
    }

    def "variadic args test"() {
        given:
        def evaluator = new Evaluator()
        def env = new HashMap<String, Value<?>>()

        def lambdaDef = """
            ((lambda (x &rest others) (+ x (car others))) 1 2 3)
        """

        when:
        Value<?> result = evaluator.evaluate(lambdaDef, env)

        then:
        result.value() == 3
    }
}
