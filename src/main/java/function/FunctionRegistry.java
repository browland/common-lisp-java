package function;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FunctionRegistry {
    private final Map<String,Function> registry = new HashMap<>();

    public FunctionRegistry() {
        registry.put("add", new Add());
        registry.put("and", new And());
        registry.put("eq", new Eq());
        registry.put("+", new Add());
        registry.put("format", new Format());
        registry.put("load", new Load());
        registry.put("list", new ListFunction());
        registry.put("getf", new GetF());
        registry.put("cons", new Cons());
        registry.put("consp", new Consp());
        registry.put("car", new Car());
        registry.put("cdr", new Cdr());
        registry.put("cadr", new Cadr());
        registry.put("=", new NumsEqual());
        registry.put("<", new LessThan());
        registry.put("macroexpand-1", new Macroexpand1());
        registry.put("rplaca", new RPlaca());
        registry.put("symbolp", new Symbolp());
        registry.put("listp", new Listp());
        registry.put("funcall", new Funcall());
    }

    public Optional<Function> findByName(String functionName) {
        return Optional.ofNullable(registry.get(functionName));
    }
}
