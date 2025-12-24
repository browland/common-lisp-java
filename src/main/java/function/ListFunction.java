package function;

import evaluator.env.Environment;
import value.ConsCellValue;
import value.Value;

import java.util.ArrayList;
import java.util.List;

public class ListFunction implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        List<Value<?>> copyOfArgs = new ArrayList<>(operands);
        return ConsCellValue.fromJavaList(copyOfArgs);
    }
}
