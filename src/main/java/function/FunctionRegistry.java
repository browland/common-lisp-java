package function;

import java.util.HashMap;
import java.util.Map;

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
    }

    public Function findByName(String functionName) {
        return registry.get(functionName);
    }
}
