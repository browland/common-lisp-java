package evaluator.env;

import value.Symbol;
import value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ScopeEnvironment {
    private final GlobalEnvironment globalEnvironment;

    public ScopeEnvironment(GlobalEnvironment globalEnvironment) {
        this.globalEnvironment = globalEnvironment;
    }

    private final Map<Symbol, Value<?>> bindings = new HashMap<>();
    private final Map<Symbol, Value<?>> functions = new HashMap<>();

    public Optional<Value<?>> getBinding(Symbol symbol) {
        if(bindings.containsKey(symbol)) {
            return Optional.of(bindings.get(symbol));
        }
        return Optional.empty();
    }

    public void setBinding(Symbol symbol, Value<?> value) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't bind, already exists: " + symbol);
        }

        bindings.put(symbol, value);
    }

    public Optional<Value<?>> getFunction(Symbol symbol) {
        if(functions.containsKey(symbol)) {
            return Optional.of(functions.get(symbol));
        }
        return Optional.empty();
    }

    public void setFunction(Symbol symbol, Value<?> function) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't bind, already exists: " + symbol);
        }

        functions.put(symbol, function);
    }
}
