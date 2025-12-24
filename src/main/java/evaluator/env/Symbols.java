package evaluator.env;

import value.Symbol;

import java.util.HashMap;
import java.util.Map;

public class Symbols {
    private final Map<String, Symbol> symbols = new HashMap<>();
    private final Map<String, Symbol> keywords = new HashMap<>();

    public Symbols() {
        Symbol nil = new Symbol("nil");
        Symbol t = new Symbol("t");
        symbols.put("nil", nil);
        symbols.put("t", t);
    }

    public Symbol internSymbol(String name) {
        Map<String, Symbol> mapToCheck = name.startsWith(":") ? keywords : symbols;
        return mapToCheck.computeIfAbsent(name, Symbol::new);
    }
}
