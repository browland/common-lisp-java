package function;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FunctionRegistry {
    private final Map<String,Function> registry = new HashMap<>();

    public FunctionRegistry() {
        registry.put("add", new Add());
        registry.put("+", new Add());
        registry.put("format", new Format());
        registry.put("load", new Load());
        registry.put("list", new ListFunction());
        registry.put("getf", new GetF());
        registry.put("cons", new Cons());
        registry.put("car", new Car());
        registry.put("=", new NumsEqual());
    }

    public Optional<Function> findByName(String functionName) {
        return Optional.ofNullable(registry.get(functionName));
    }
}
