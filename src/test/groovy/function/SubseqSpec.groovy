package function

import evaluator.env.Environment
import spock.lang.Specification
import value.ConsCell
import value.ConsCellValue
import value.FunctionValue
import value.IntegerValue
import value.StringValue

class SubseqSpec extends Specification {
    def "basic test"() {
        given:
        Subseq subseq = new Subseq()
        StringValue inputValue = new StringValue("hello world")
        IntegerValue startValue = new IntegerValue(2)
        IntegerValue endValue = new IntegerValue(5)
        Environment env = new Environment()

        when:
        def result = subseq.apply(List.of(inputValue, startValue, endValue), env)

        then:
        StringValue expected = new StringValue("llo")
        result == expected
    }
}
