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
    private int minOffset = 0;

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

    // Augment the current lexical scope with a new set of bindings, e.g. when we enter a `let` form within a function.
    // In this case the let bindings occupy a lexical scope 'within' the lexical scope of the function.
    // We maintain minOffset so that any inner lexical scope starts from the bottom of the enclosing one.
    public void pushStackOffsets(Map<String,Integer> stackOffsets) {
        stackOffsetsStack.push(stackOffsets);
        for (int offset : stackOffsets.values()) {
            if (offset < minOffset) {
                minOffset = offset;
            }
        }
    }

    public int getMinOffset() {
        return minOffset;
    }

    public int getStackBytes() {
        // each stack slot is 8 bytes
        int stackSlots = 0;

        Iterator<Map<String,Integer>> iter = stackOffsetsStack.descendingIterator();
        while (iter.hasNext()) {
            Map<String,Integer> stackOffsets = iter.next();
            stackSlots += stackOffsets.size();
        }

       return (int)(16 * Math.ceil(stackSlots/2f));
    }
}
