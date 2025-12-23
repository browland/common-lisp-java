package evaluator.env;

import value.Value;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GlobalEnvironment {
    private final Map<String, Value<?>> builtInGlobals = new HashMap<>();
    private final Map<String, Value<?>> globalVariables = new HashMap<>();
    private final Map<String, Value<?>> macros = new HashMap<>();

    private final Map<String, Value<?>> builtInFunctions = new HashMap<>();
    private final Map<String, Value<?>> functions = new HashMap<>();

    public Optional<Value<?>> getValue(String symbolName) {
        if(builtInGlobals.containsKey(symbolName)) {
            return Optional.of(builtInGlobals.get(symbolName));
        }
        else if(globalVariables.containsKey(symbolName)) {
            return Optional.of(globalVariables.get(symbolName));
        }
        else if(functions.containsKey(symbolName)) {
            return Optional.of(functions.get(symbolName));
        }
        return Optional.empty();
    }

    public void setGlobal(String name, Value<?> value) {
        if(builtInGlobals.containsKey(name)) {
            throw new RuntimeException("Can't set a built-in global " + name);
        }
        globalVariables.put(name, value);
    }


    public Optional<Value<?>> getMacro(String macroName) {
        if(macros.containsKey(macroName)) {
            return Optional.of(macros.get(macroName));
        }
        return Optional.empty();
    }

    public void setMacro(String name, Value<?> macro) {
        macros.put(name, macro);
    }

    public Optional<Value<?>> getFunction(String functionName) {
        if(builtInFunctions.containsKey(functionName)) {
            return Optional.of(builtInFunctions.get(functionName));
        }
        if(functions.containsKey(functionName)) {
            return Optional.of(functions.get(functionName));
        }

        return Optional.empty();
    }

    public void setFunction(String name, Value<?> function) {
        if(builtInFunctions.containsKey(name)) {
            throw new RuntimeException("Can't set a function with existing built-in function " + name);
        }
        functions.put(name, function);
    }

    public boolean isReserved(String name) {
        return builtInGlobals.containsKey(name) || builtInFunctions.containsKey(name);
    }
}
