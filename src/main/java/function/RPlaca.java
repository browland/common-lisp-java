package function;

import evaluator.env.Environment;
import value.ConsCell;
import value.ConsCellValue;
import value.Value;

import java.util.List;

public class RPlaca implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        if(operands.size() == 2) {
            Value<?> val1 = operands.get(0);
            Value<?> replacementCar = operands.get(1);

            if(val1 instanceof ConsCellValue consCellValue) {
                replaceCar(consCellValue, replacementCar);
                return consCellValue;
            }
            else {
                throw new IllegalArgumentException("Expect args cons, replacement car");
            }
        }
        else {
            throw new IllegalArgumentException("Expect 2 args for rplaca");
        }
    }

    private void replaceCar(ConsCellValue consCellValue, Value<?> newCar) {
        ConsCell cons = consCellValue.getValue();
        cons.setCar(newCar);
    }
}
