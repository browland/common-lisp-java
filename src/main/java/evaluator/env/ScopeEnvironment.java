package evaluator.env;

import value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScopeEnvironment {
    private GlobalEnvironment globalEnvironment;

    public ScopeEnvironment(GlobalEnvironment globalEnvironment) {
        this.globalEnvironment = globalEnvironment;
    }

    private final Map<String, Value<?>> bindings = new HashMap<>();
    private final Map<String, Value<?>> functions = new HashMap<>();

    public Optional<Value<?>> getBinding(String name) {
        if(bindings.containsKey(name)) {
            return Optional.of(bindings.get(name));
        }
        return Optional.empty();
    }

    public void setBinding(String name, Value<?> value) {
        if(globalEnvironment.isReserved(name)) {
            throw new RuntimeException("Can't bind, already exists: " + name);
        }

        bindings.put(name, value);
    }

    public Optional<Value<?>> getFunction(String name) {
        if(functions.containsKey(name)) {
            return Optional.of(functions.get(name));
        }
        return Optional.empty();
    }

    public void setFunction(String name, Value<?> function) {
        if(globalEnvironment.isReserved(name)) {
            throw new RuntimeException("Can't bind, already exists: " + name);
        }

        functions.put(name, function);
    }
}
