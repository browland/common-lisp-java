package evaluator.special;

import value.Symbol;

public class GoException extends RuntimeException {
    private final Symbol symbol;

    public GoException(Symbol symbol) {
        this.symbol = symbol;
    }

    public Symbol getSymbol() {
        return symbol;
    }
}
