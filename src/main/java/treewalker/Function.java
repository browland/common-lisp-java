package treewalker;

import java.util.Map;

public class Function {
    private final String symbolStringName;
    private final Map<String, Integer> stackOffsets;

    public Function(String symbolStringName, Map<String,Integer> stackOffsets) {
        this.symbolStringName = symbolStringName;
        this.stackOffsets = stackOffsets;
    }

    public String getSymbolStringName() {
        return symbolStringName;
    }

    public Map<String,Integer> getStackOffsets() {
        return stackOffsets;
    }

    public boolean containsBinding(String bindingName) {
        return stackOffsets.containsKey(bindingName);
    }
}
