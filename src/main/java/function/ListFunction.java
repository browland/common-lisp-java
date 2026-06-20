package function;

import evaluator.env.Environment;
import value.ConsCellValueFactory;
import value.Value;

import java.util.ArrayList;
import java.util.List;

public class ListFunction implements Function {

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        List<Value<?>> copyOfArgs = new ArrayList<>(operands);
        return ConsCellValueFactory.fromJavaList(copyOfArgs);
    }
}
