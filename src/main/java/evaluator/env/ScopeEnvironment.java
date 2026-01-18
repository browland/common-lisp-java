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

    private final Map<Symbol, Value<?>> variables = new HashMap<>();
    private final Map<Symbol, Value<?>> functions = new HashMap<>();
    private final Map<Symbol, Value<?>> blocks = new HashMap<>();

    public Optional<Value<?>> getVariable(Symbol symbol) {
        if(variables.containsKey(symbol)) {
            return Optional.of(variables.get(symbol));
        }
        return Optional.empty();
    }

    public void setVariable(Symbol symbol, Value<?> value) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't bind, already exists: " + symbol);
        }

        variables.put(symbol, value);
    }

    public Optional<Value<?>> getFunction(Symbol symbol) {
        if(functions.containsKey(symbol)) {
            return Optional.of(functions.get(symbol));
        }
        return Optional.empty();
    }

    public Optional<Value<?>> getBlock(Symbol symbol) {
        if(blocks.containsKey(symbol)) {
            return Optional.of(blocks.get(symbol));
        }
        return Optional.empty();
    }

    public void setFunction(Symbol symbol, Value<?> function) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't bind, already exists: " + symbol);
        }

        functions.put(symbol, function);
    }

    public void setBlock(Symbol symbol, Value<?> block) {
        if(globalEnvironment.isReserved(symbol)) {
            throw new RuntimeException("Can't bind, already exists: " + symbol);
        }

        blocks.put(symbol, block);
    }
}
