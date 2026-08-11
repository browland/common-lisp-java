package compiler.treewalker;

import java.util.*;

/**
 * Represents the stack frame for an asm function.
 * The stack offsets only work within a single asm function frame, so a new Function should be created once entering a
 * new asm function.
 */
public class Function {
    private final String symbolStringName;
    private final Deque<Map<String,Integer>> stackOffsetsStack;

    public Function(String symbolStringName, Map<String,Integer> stackOffsets) {
        this.symbolStringName = symbolStringName;
        this.stackOffsetsStack = new LinkedList<>();
        this.stackOffsetsStack.push(stackOffsets);
    }

    public String getSymbolStringName() {
        return symbolStringName;
    }

    public Optional<Integer> getClosestOffset(String symbolName) {
        Iterator<Map<String,Integer>> iter = stackOffsetsStack.descendingIterator();
        while (iter.hasNext()) {
            Map<String,Integer> stackOffsets = iter.next();
            if (stackOffsets.containsKey(symbolName)) {
                return Optional.of(stackOffsets.get(symbolName));
            }
        }

        return Optional.empty();
    }

    public boolean containsBinding(String bindingName) {
        Iterator<Map<String,Integer>> iter = stackOffsetsStack.descendingIterator();
        while (iter.hasNext()) {
            Map<String,Integer> stackOffsets = iter.next();
            if (stackOffsets.containsKey(bindingName)) {
                return true;
            }
        }

        return false;
    }

    public void pushStackOffsets(Map<String,Integer> stackOffsets) {
        stackOffsetsStack.push(stackOffsets);
    }
}
