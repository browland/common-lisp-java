package function;

import evaluator.env.Environment;
import value.Value;

import java.util.List;

public interface Function {

    Value<?> apply(List<Value<?>> operands, Environment environment);
}
