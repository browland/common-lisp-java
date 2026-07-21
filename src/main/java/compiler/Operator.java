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
            case "+": return new Operator(OperatorName.ADD, OperatorType.FUNCTION);
            case "if": return new Operator(OperatorName.IF, OperatorType.SPECIAL_FORM);
            default: throw new UnsupportedOperationException("unsupported operator");
        }
    }

}
