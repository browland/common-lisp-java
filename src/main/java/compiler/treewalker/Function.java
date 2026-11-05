package compiler.treewalker;

import java.util.*;

/**
 * Represents the stack frame for an asm function.  An asm function is generated for each of:
 * 1. The main flow itself, outside of any user-defined function or closure.
 * 2. A user-defined function (via defun)
 * 3. A user-defined closure (via lambda expression)
 *
 * The stack offsets only work within a single asm function frame, so a new Function should be created once entering a
 * new asm function.
 */
public class Function {
    // TODO this conflicts a bit with the notion that this class represents an asm function frame, as this is the lisp symbol for the function being called.
    private final String symbolStringName;
    // Represents our lexical scope of symbols to their offsets on the stack
    private final Deque<Map<String,Integer>> stackOffsetsStack;
    private int minOffset = 0;
    private boolean variadicArgs;

    public Function(String symbolStringName, Map<String,Integer> stackOffsets) {
        this.symbolStringName = symbolStringName;
        this.stackOffsetsStack = new LinkedList<>();
        this.stackOffsetsStack.push(stackOffsets);
    }

    public Function(String symbolStringName, boolean variadicArgs) {
        this.symbolStringName = symbolStringName;
        this.stackOffsetsStack = new LinkedList<>();
        this.stackOffsetsStack.push(Map.of());
        this.variadicArgs = variadicArgs;
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

    public void pushStackOffsetFrame() {
        stackOffsetsStack.push(new HashMap<>());
    }

    public void pushStackOffset(String name, int offset) {
        Map<String,Integer> stackOffsets = stackOffsetsStack.peek();
        stackOffsets.put(name, offset);

        if (offset < minOffset) {
            minOffset = offset;
        }
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

       return (int)(16 * Math.ceil((stackSlots)/2f));
    }

    public boolean isVariadicArgs() {
        return variadicArgs;
    }
}
