package compiler;

public class Operator {
    private final OperatorType operatorType;

    public Operator(OperatorType operatorType) {
        this.operatorType = operatorType;
    }

    public OperatorType getOperator() {
        return operatorType;
    }

    public static Operator fromSymbol(String symbol) {
        switch (symbol) {
            case "+": return new Operator(OperatorType.ADD);
            default: throw new UnsupportedOperationException("unsupported operator");
        }
    }

}
