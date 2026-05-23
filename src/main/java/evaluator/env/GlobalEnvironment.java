package evaluator.env;

import value.Symbol;
import value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalEnvironment {
    private final Map<Symbol, Value<?>> builtInVariables = new HashMap<>();
    private final Map<Symbol, Value<?>> globalVariables = new HashMap<>();
    private final Map<Symbol, Value<?>> macros = new HashMap<>();

    private final Map<Symbol, Value<?>> builtInFunctions = new HashMap<>();
    private final Map<Symbol, Value<?>> functions = new HashMap<>();

    public GlobalEnvironment() {
        // Set up built-in symbols
        Symbol t = Symbols.t();
        builtInVariables.put(t, Value.t());

        Symbol nil = Symbols.nil();
        builtInVariables.put(nil, Value.nil());
    }

    public Optional<Value<?>> getVariable(Symbol symbol) {
        if(builtInVariables.containsKey(symbol)) {
            return Optional.of(builtInVariables.get(symbol));
        }
        else if(globalVariables.containsKey(symbol)) {
            return Optional.of(globalVariables.get(symbol));
        }
        return Optional.empty();
    }

    public void setVariable(Symbol symbol, Value<?> value) {
        if(builtInVariables.containsKey(symbol)) {
            throw new RuntimeException("Can't set a built-in global " + symbol);
        }
        globalVariables.put(symbol, value);
    }

    @Deprecated
    public void setGlobal(Symbol symbol, Value<?> value) {
        if(builtInVariables.containsKey(symbol)) {
            throw new RuntimeException("Can't set a built-in global " + symbol);
        }
        globalVariables.put(symbol, value);
    }

    public Optional<Value<?>> getMacro(Symbol macroSymbol) {
        if(macros.containsKey(macroSymbol)) {
            return Optional.of(macros.get(macroSymbol));
        }
        return Optional.empty();
    }

    public void setMacro(Symbol symbol, Value<?> macro) {
        macros.put(symbol, macro);
    }

    public Optional<Value<?>> getFunction(Symbol symbol) {
        if(builtInFunctions.containsKey(symbol)) {
            return Optional.of(builtInFunctions.get(symbol));
        }
        if(functions.containsKey(symbol)) {
            return Optional.of(functions.get(symbol));
        }

        return Optional.empty();
    }

    public void setFunction(Symbol symbol, Value<?> function) {
        if(builtInFunctions.containsKey(symbol)) {
            throw new RuntimeException("Can't set a function with existing built-in function " + symbol);
        }
        functions.put(symbol, function);
    }

    public boolean isReserved(Symbol symbol) {
        return builtInVariables.containsKey(symbol) || builtInFunctions.containsKey(symbol);
    }
}
