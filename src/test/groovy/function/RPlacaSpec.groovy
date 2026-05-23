package function

import evaluator.env.Environment
import spock.lang.Specification
import value.ConsCell
import value.ConsCellValue
import value.IntegerValue

class RPlacaSpec extends Specification {
    def "basic test"() {
        given:
        RPlaca rPlaca = new RPlaca()
        ConsCell cons = ConsCell.fromValue(new IntegerValue(1))
            .push(new IntegerValue(2));
        IntegerValue newCar = new IntegerValue(3)
        Environment env = new Environment()

        when:
        def result = rPlaca.apply(List.of(cons.wrap(), newCar), env)

        then:
        ConsCellValue expected = ConsCell.fromValue(new IntegerValue(1))
                .push(new IntegerValue(3))
                .wrap()
        result == expected
    }
}
