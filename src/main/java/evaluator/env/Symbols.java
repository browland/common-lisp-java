package evaluator.env;

import value.Symbol;

import java.util.HashMap;
import java.util.Map;

public class Symbols {
    private static final Map<String, Symbol> symbols = new HashMap<>();
    private static final Map<String, Symbol> keywords = new HashMap<>();

    static {
        Symbol nil = new Symbol("nil");
        Symbol t = new Symbol("t");
        symbols.put("nil", nil);
        symbols.put("t", t);
    }

    public static Symbol internSymbol(String name) {
        Map<String, Symbol> mapToCheck = name.startsWith(":") ? keywords : symbols;
        return mapToCheck.computeIfAbsent(name, Symbol::new);
    }

    public static Symbol t() {
        return symbols.get("t");
    }

    public static Symbol nil() {
        return symbols.get("nil");
    }
}
