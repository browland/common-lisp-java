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
        registry.put("*", new Multiply());
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
        registry.put(">", new GreaterThan());
        registry.put("macroexpand-1", new Macroexpand1());
        registry.put("rplaca", new RPlaca());
        registry.put("symbolp", new Symbolp());
        registry.put("listp", new Listp());
        registry.put("funcall", new Funcall());
        registry.put("random", new Random());
        registry.put("assoc", new Assoc());
        registry.put("null", new Null());
        registry.put("mapcar", new Mapcar());
        registry.put("string=", new StringEqual());
        registry.put("evenp", new Evenp());
        registry.put("values", new Values());
        registry.put("floor", new Floor());
    }

    public Optional<Function> findByName(String functionName) {
        return Optional.ofNullable(registry.get(functionName));
    }
}
