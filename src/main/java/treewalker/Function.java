package treewalker;

public class Function {
    private final String asmFunctionName;
    private final String symbolStringName;

    public Function(String asmFunctionName, String symbolStringName) {
        this.asmFunctionName = asmFunctionName;
        this.symbolStringName = symbolStringName;
    }

    public String getSymbolStringName() {
        return symbolStringName;
    }
}
