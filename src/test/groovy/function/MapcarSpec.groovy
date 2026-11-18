package function

import evaluator.env.Environment
import spock.lang.Specification
import value.ConsCell
import value.ConsCellValue
import value.FunctionValue
import value.IntegerValue

class MapcarSpec extends Specification {
    def "basic test"() {
        given:
        Mapcar mapcar = new Mapcar()
        FunctionValue onePlusFunctionVal = new FunctionValue(new OnePlus())
        ConsCell cons = ConsCell.fromValue(new IntegerValue(1))
            .push(new IntegerValue(2));
        Environment env = new Environment()

        when:
        def result = mapcar.apply(List.of(onePlusFunctionVal, cons.wrap()), env)

        then:
        ConsCellValue expected = ConsCell.fromValue(new IntegerValue(2))
                .push(new IntegerValue(3))
                .wrap()
        result == expected
    }
}
