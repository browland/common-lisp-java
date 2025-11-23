package function;

import evaluator.Value;

import java.util.List;
import java.util.Map;

public interface Function {

    Value<?> apply(List<Value<?>> operands, Map<String,Value<?>> environment);
}
