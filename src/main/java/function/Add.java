package function;

import evaluator.Value;
import evaluator.ValueType;

import java.util.List;
import java.util.Map;

public class Add implements Function {

    @Override
    public Value<?> apply(List<String> operands, Map<String,String> environment) {
        // terrible assumption for now that operands are all Atoms and their string values parse as integers ... can overflow ... etc etc.
        int result = 0;

        for(String operand : operands) {
            try {
                int intOperand = Integer.parseInt(operand);
                result += intOperand;
            }
            catch(NumberFormatException e) {
                // todo for now we naively assume this is a symbol
                String symbolValueFromEnvironment = environment.get(operand);
                try {
                    result += Integer.parseInt(symbolValueFromEnvironment);
                }
                catch(Exception e2) {
                    throw new RuntimeException(e2);

                }
            }
        }

        return new Value<>(Integer.toString(result), ValueType.LITERAL);
    }
}
