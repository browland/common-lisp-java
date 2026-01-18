package evaluator.special;

import value.Symbol;
import value.Value;

public class ReturnFromException extends RuntimeException {
    private final Symbol blockName;
    private final Value<?> returnValue;

    public ReturnFromException(Symbol blockName, Value<?> returnValue) {
        super();
        this.blockName = blockName;
        this.returnValue = returnValue;
    }

    public Symbol getBlockName() {
        return blockName;
    }

    public Value<?> getReturnValue() {
        return returnValue;
    }
}
