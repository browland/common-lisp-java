package compiler;

public class Operator {
    private final OperatorName operatorName;
    private final OperatorType operatorType;

    public Operator(OperatorName operatorName, OperatorType operatorType) {
        this.operatorName = operatorName;
        this.operatorType = operatorType;
    }

    public OperatorName getOperatorName() {
        return operatorName;
    }

    public OperatorType getOperatorType() {
        return operatorType;
    }

    public static Operator fromSymbol(String symbol) {
        switch (symbol) {
            case "+", "add": return new Operator(OperatorName.ADD, OperatorType.FUNCTION);
            case "if": return new Operator(OperatorName.IF, OperatorType.SPECIAL_FORM);
            case "defvar": return new Operator(OperatorName.DEFVAR, OperatorType.SPECIAL_FORM);
            case "defun": return new Operator(OperatorName.DEFUN, OperatorType.SPECIAL_FORM);

            // not a built-in operator; generate asm to look up
            // for now we just assume a function has been defined and we treat it as such but should have more guarantees
            // e.g. by keeping a table of defined functions around purely in the compiler as we walk the tree
            default: throw new UnsupportedOperationException("unsupported operator");
        }
    }

}
