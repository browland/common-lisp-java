package parser

import spock.lang.Specification

class FormBuilderSpec extends Specification {

    def "this test goes too far and checks the result of parsing and applying the built Form, easier that way for now"() {
        given:
        when:
        def nodes = FormTreeBuilder.parse(formText)
        def form = FormBuilder.build(nodes)
        def result = form.apply()
        then:
        result == expectedResult

        where:
        formText                    || expectedResult
        "((lambda (x) (+ x 1)) 1)"  || "2"
    }
}
