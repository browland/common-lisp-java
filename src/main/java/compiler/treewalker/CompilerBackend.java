package compiler.treewalker;

import compiler.AsmGenerator;
import compiler.Namespace;
import syntaxtree.Atom;
import syntaxtree.Node;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CompilerBackend {
    private final AsmGenerator asmGenerator = new AsmGenerator();

    /*
     Used for keeping track of stack offsets as we create new scopes.
     */
    private final Deque<Function> functionStack = new LinkedList<>();

    /*
     The functionsMap is useful for at least one thing:
     1. Mapping the Lisp symbol for an operator to the asm function name.  E.g. we can't create an asm routine called "+"
        so when we see the + operator we need to look its asm name "add".
     */
    private final Map<String,Function> functionsMap = new HashMap<>();

    /*
     Used to avoid double-allocating a symbol, as we create the entry statically.
     */
    private final Set<String> definedSymbolsInNamespace = new HashSet<>();

    /*
     Symbols which have a value in the data namespace.
     */
    private final Set<String> declaredVariableNames = new HashSet<>();

    /*
     * Each Map in the Deque represents a tagbody in the current lexical scope; multiple Maps imply nested tagbodys.
     * If the Deque is non-empty this means we're in at least one tagbody.
     */
    private final Deque<Map<String,String>> tagToAsmLabelStack = new LinkedList<>();

    /*
     * We keep track of stack bytes still allocated "beyond" each tag - this is so if we 'go' to that tag we know
     * how many bytes we need to deallocate (since the 'go' could be issued within a stack of special operators such
     * as if (but not within a function body as that would leave the lexical scope of the tagbody).  When the 'go' is
     * issued, we jump straight to the tag, so we need to take care of stack cleanup.
     * This is a bit artificial as the value for each tag in a tagbody will be the same, but the tags give us a clear
     * 'anchor point' for lookup and we don't have anything like that for the tagbody itself.
     * Each Map in the Deque represents a tagbody in the current lexical scope; multiple Maps imply nested tagbodys.
     * If the Deque is non-empty this means we're in at least one tagbody.
     */
    private final Deque<Map<String,Integer>> tagToStackBytesAllocated = new LinkedList<>();

    public CompilerBackend() {
        // Set up built-in functions.  We only *really* need these mappings so we can look up the asm name for the Lisp
        // operator name.
        functionsMap.put("add", new Function("add", false));
        functionsMap.put("+", new Function("add", false));
        functionsMap.put("cons", new Function("cons", false));
        functionsMap.put("list", new Function("list", true));
        functionsMap.put("car", new Function("car", false));
        functionsMap.put("cdr", new Function("cdr", false));
        functionsMap.put("mapcar", new Function("mapcar", false));
        functionsMap.put("oneplus", new Function("oneplus", false));
        functionsMap.put("eq", new Function("eq", false));
    }

    public void startProgram() {
        asmGenerator.initMainFunction();
        Function topLevelFunction = new Function("_default_", Map.of());
        functionStack.push(topLevelFunction);
    }

    public void endProgram() {
        asmGenerator.printResultAndCleanUpMainFunction();
        BufferedWriter bw;
        try {
            bw = new BufferedWriter(new FileWriter("./src/main/asm/my-asm.s"));
            asmGenerator.dumpAsm(bw);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleSymbolOperand(String symbol, Namespace namespace) {
        Function currentScope = functionStack.peek();
        if (currentScope.containsBinding(symbol) && namespace == Namespace.VARIABLE) {
            int stackOffset = currentScope.getClosestOffset(symbol).orElseThrow(() -> new IllegalArgumentException("symbol %s not in scope".formatted(symbol)));
            asmGenerator.loadOperandFromStackOffsetIntoRegister(stackOffset, 0);
        }
        else {
            // check our mappings to stack offsets first (bindings for the current function)
            asmGenerator.generateSymbolLookup(symbol, namespace);
        }
    }

    public void handleSymbolOperandNoLookup(String symbol) {
        // TODO okay that we only interact with sym table and bindings don't come into it - I think ok?
        asmGenerator.loadSymbolTaggedPtr(symbol);
    }

    public void handleIntOperand(IntAtom intAtom) {
        System.out.printf("Storing int literal operand %d to register 0\n", intAtom.getValue());
        asmGenerator.writeLongToRegister(0, intAtom.getFixNum());
    }

    public void writeLongToRegister(long value, int regNum) {
        System.out.printf("Storing int literal operand %d to register %d\n", value, regNum);
        asmGenerator.writeLongToRegister(regNum, value);
    }

    public void startFunction(String name, boolean isLambda) {
        asmGenerator.startFunctionDef();
        if (!isLambda) {
            initialiseFunctionSymbol(name);
        }
        functionPrologue(name);
    }

    // returns true if was created; false if already existed.
    public boolean initialiseSymbol(String symbol) {
        if (definedSymbolsInNamespace.contains(symbol)) {
            return false;
        }
        asmGenerator.initSymbolTableEntry(symbol);
        definedSymbolsInNamespace.add(symbol);
        return true;
    }

    public void initialiseFunctionSymbol(String symbol) {
        asmGenerator.addFunctionToSymbolTable(symbol);
    }

    public void functionPrologue(String name) {
        asmGenerator.initFunction(name);
    }

    public int reserveStackForVariables(int numVariables) {
        int stackBytes = determineStackBytes(numVariables);
        System.out.printf("Reserving %d bytes of stack\n", stackBytes);
        asmGenerator.reserveSpaceOnStack(stackBytes);
        return stackBytes;
    }

    public void freeStackForVariables(int numVariables) {
        int stackBytes = determineStackBytes(numVariables);
        System.out.printf("Freeing %d bytes of stack\n", stackBytes);
        asmGenerator.freeSpaceOnStack(stackBytes);
    }

    public void freeStackBytes(int bytes) {
        if (bytes <= 0) {
            throw new RuntimeException("Trying to free stack bytes: " + bytes);
        }
        System.out.printf("Freeing %d bytes of stack\n", bytes);
        asmGenerator.freeSpaceOnStack(bytes);
    }

    public void setUpFunctionBindings(String functionName, List<String> bindingNameList) {
        int pos = 0;
        Map<String, Integer> stackOffsets = new HashMap<>();
        int stackBytes = determineStackBytes(bindingNameList.size());

        for (String bindingName : bindingNameList) {
            System.out.printf("defun: storing binding for %s to stack%n", bindingName);
            asmGenerator.storeOperandFromRegisterToStack(pos);
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            int stackOffset = -1*stackBytes + (pos*8);
            stackOffsets.put(bindingName, stackOffset);
            pos++;
        }

        // Store stack offsets on Function so we can pass them around with relevant context
        Function newFunctionScope = new Function(functionName, stackOffsets);
        functionStack.push(newFunctionScope);

        // add this function to our compile-time Map
        functionsMap.put(functionName, newFunctionScope);
    }

    public void endFunction(Function function) {
        // Automatically free stack based on passed function scope
        int stackBytes = function.getStackBytes();

        System.out.printf("lambda: freeing stack bytes: %d\n",stackBytes);
        asmGenerator.freeSpaceOnStack(stackBytes);
        asmGenerator.endFunction();
        asmGenerator.endFunctionDef();
    }

    public void storeResultToSymbolValue(String symbolName) {
        // TODO we're coupling to aarch64 here
        asmGenerator.writeRegisterToSymbolValue(0, symbolName);
    }

    public void storeResultToStack(int variableIndex) {
        System.out.printf("Storing result from x0 to stack at index %d\n", variableIndex);
        asmGenerator.storeResultToStack(variableIndex);
    }

    public void storeResultToStackFPOffset(int fpOffset) {
        asmGenerator.storeOperandFromRegisterToStackFPOffset(0, fpOffset);
    }

    public void pushStackOffsetFrame() {
        // for let bindings; nested frames of stack offsets within current function stack frame!
        Function currentScope = functionStack.peek();
        currentScope.pushStackOffsetFrame();
    }

    public void storeResultingLetBindingToStack(int numBindings, int bindingIndex, String bindingName) {
        System.out.println("let: copying operand from register %d to stack at sp+%d\n".formatted(bindingIndex, bindingIndex*8));
        asmGenerator.storeResultToStack(bindingIndex);
        Function currentScope = functionStack.peek();

        int stackBytes = determineStackBytes(numBindings);

        int minOffset = currentScope.getMinOffset();

        int stackOffset;
        if (bindingIndex == 0) {
            // store first let binding at bottom of newly reserved stack (prev. min offset minus newly reserved stack bytes)
            stackOffset = minOffset - stackBytes;
        }
        else {
            // store subsequent let bindings in appropriate slot above previous ones (min offset will have changed when
            // storing the 0th let binding).
            stackOffset = minOffset + (bindingIndex*8);
        }

        currentScope.pushStackOffset(bindingName, stackOffset);

        System.out.println("let: pushed stack offset, binding name %s, index %d, offset %d".formatted(bindingName, bindingIndex, stackOffset));
    }

    public void typeCheckResultIsSymbol() {
        asmGenerator.generateTypeCheckForSymbol();
    }

    public void loadVariableFromStackIntoRegister(int stackPos, int regNum) {
        System.out.printf("Loading operand from stack pos %d to reg num %d\n", stackPos, regNum);
        asmGenerator.loadOperandFromStackIntoRegister(stackPos, regNum);
    }

    public void moveStackPointerToRegister(int offsetFromStackPointer, int regNum) {
        System.out.printf("Moving address of stack pointer plus offset %d to register %d\n", offsetFromStackPointer, regNum);
        asmGenerator.moveStackPointerToRegister(offsetFromStackPointer, regNum);
    }

    public void resultWasT() {
        asmGenerator.generateCheckForT();
    }

    public void jumpToLabelIfResultNonZero(String label) {
        asmGenerator.generateJumpInstructionForNonZeroReturnValue(label);
    }

    public void generateLabel(String label) {
        asmGenerator.generateLabel(label);
    }

    public void jumpToLabel(String label) {
        asmGenerator.generateUnconditionalJump(label);
    }

    public void createClosure(String lambdaAsmName, List<String> capturedVariables, List<String> bindingNames) {
        int numCaptures = Math.max(capturedVariables.size(), 8);  // ensure we don't alloc 0 byte array;
        asmGenerator.mkCaptures(numCaptures);

        // captures ptr is now in x0

        Function currentFunctionScope = functionStack.peek();
        for(int captureIndex=0; captureIndex<capturedVariables.size(); captureIndex++) {
            // Copies captured variable at frame pointer offset in current lexical scope to the next position in the heap-allocated captures array.
            String capturedVariable = capturedVariables.get(captureIndex);
            int sourceOffsetInThisLexicalScope = currentFunctionScope.getClosestOffset(capturedVariable)
                    .orElseThrow(() -> new IllegalStateException("Have captured var %s but can't find it in enclosing scope!".formatted(capturedVariable)));
            System.out.println("adding capture for var %s in lexical scope at offset %d".formatted(capturedVariable, sourceOffsetInThisLexicalScope) );
            asmGenerator.addCapture(sourceOffsetInThisLexicalScope, captureIndex);
            // todo captures ptr is now in x0 again as it's returned from add_capture
        }

        // heap allocate this closure object - holds function ptr and captures array
        // This ends up being the value of this lambda evaluation.  If this seems counter-intuitive, the remaining code
        // from here switches to other `AsmContext`s in order to write the symbol table entry and asm function, out of
        // the main flow.
        asmGenerator.putClosure(lambdaAsmName, bindingNames);
    }

    /**
     * Closure stack layout:
     *
     * +----------------------------+
     *      +-- Saved caller's LR      --+ (as for any function stack frame)
     *      +-- Saved caller's FP      --+ (as for any function stack frame)
     * FP-> +-- (Optional empty slot)  --+ (to achieve 16 byte alignment if needed)
     *      +-- Capture 2              --+ (capture 2 ...)
     *      +-- Capture 1              --+ (capture 1 which we'll copy from the heap)
     *      +-- Binding 2              --+ (binding 2 ...)
     *      +-- Binding 1              --+ (binding 1 passed at apply time)
     * SP-> +-- Closure ptr            --+ (at bottom of stack to keep separate from bindings/captures; also we always have one of these)
     * +----------------------------+
     */
    public Function setUpClosureFunctionStack(List<String> capturedVariables, List<Node> bindingsList, String lambdaFunctionName) {
        // TODO dupe code
        int numBindings = bindingsList.size();
        int numCaptures = capturedVariables.size();

        // We need 8 bytes for each binding, capture and the captures ptr, but ensure we always reserve a multiple of 16 bytes
        // This moves sp down to bottom of scheme shown above.
        final int stackBytes = (int)(16 * Math.ceil((numBindings + numCaptures + 1)/2f));
        System.out.printf("lambda: allocating %d bytes of stack%n", stackBytes);
        asmGenerator.reserveSpaceOnStack(stackBytes);

        // Deal with closure ptr first.  The closure ptr is always passed directly after the bindings and we can get
        // the captures ptr from that via a deref.
        int closurePtrRegNum = numBindings;
        int closurePtrFPOffset = -1 * stackBytes;  // closure ptr goes to bottom of our stack frame
        System.out.printf("lambda: storing closure ptr from reg %d to stack at FP offset %d%n", closurePtrRegNum, closurePtrFPOffset);
        asmGenerator.storeOperandFromRegisterToStackFPOffset(closurePtrRegNum, closurePtrFPOffset);

        // TODO we're storing the closure ptr in the stack offsets map, against the name of the closure asm function
        //      We're doing this so we clean up the stack properly (which is based on Function.getStackBytes() which
        //      in turn looks at the offsets map.)  Will this cause problems if we e.g. have a binding with same name as
        //      closure?
        Map<String, Integer> closureFunctionFPOffsets = new HashMap<>();
        closureFunctionFPOffsets.put(lambdaFunctionName, closurePtrFPOffset);

        int stackPos = 1;  // Our next stack slot is 1 higher than the closure ptr we just stored
        int framePointerOffset;
        for (int operandNum = 0; operandNum< bindingsList.size(); operandNum++) {
            Node bindingNode = bindingsList.get(operandNum);
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            framePointerOffset = -1*stackBytes + (stackPos*8);
            System.out.printf("lambda: storing binding for %s from reg %d to stack at FP offset %d%n", symbolAtom.value(), operandNum, framePointerOffset);
            asmGenerator.storeOperandFromRegisterToStackFPOffset(operandNum, framePointerOffset);
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            closureFunctionFPOffsets.put(symbolAtom.value(), framePointerOffset);
            stackPos++;
        }

        for (int captureIndex=0; captureIndex<capturedVariables.size(); captureIndex++) {
            String capturedVariable = capturedVariables.get(captureIndex);

            // continue with next stack offset for captured variable; this has a bit of a smell perhaps.  We work out the
            // stack pos within storeOperandFromRegisterToStack() relative to the stack pointer, but repeat the calculation
            // here relative to the frame pointer (as it's stable over time).
            framePointerOffset = -1*stackBytes + (stackPos*8);

            System.out.printf("lambda: loading closure ptr from stack at FP offset %d%n", closurePtrFPOffset);
            asmGenerator.loadCapturedVariable(captureIndex, closurePtrFPOffset);
            System.out.printf("lambda: storing capture for value of %s from x0 (after loading it there) to stack at FP offset %d%n", capturedVariable, framePointerOffset);
            asmGenerator.storeOperandFromRegisterToStackFPOffset(0, framePointerOffset);

            closureFunctionFPOffsets.put(capturedVariable, framePointerOffset);

            stackPos++;
        }

        // Store stack offsets on Function; needed in walkTree() to generate instructions for entirety of lambda body.
        Function function = new Function(lambdaFunctionName, closureFunctionFPOffsets);

        functionStack.push(function);

        // add this function to our compile-time Map
        functionsMap.put(lambdaFunctionName, function);

        return function;
    }

    public void writeComment(String comment) {
        asmGenerator.writeComment(comment);
    }

    public void untagAndAccessFunctionPointer() {
        asmGenerator.untagAndAccessFunctionPtr();
    }

    // Expects the name starting with underscore
    public void callFunction(String functionName) {
        asmGenerator.callFunction(functionName);
    }

    public void callFunction(int functionPtrRegister) {
        System.out.printf("Calling function ptr in register %d\n", functionPtrRegister);
        asmGenerator.callFunction(functionPtrRegister);
    }

    public void closureToFunctionPtr() {
        asmGenerator.closureToFunctionPtr();
    }

    public Function findFunction(String symbol) {
        return functionsMap.get(symbol);
    }

    public Set<String> getDeclaredFunctionNames() {
        return functionsMap.keySet();
    }

    private int determineStackBytes(int numVariables) {
        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        return (int)(16 * Math.ceil(numVariables/2f));
    }

    public Set<String> getDeclaredVariableNames() {
        return declaredVariableNames;
    }

    public void recordVariableExists(String symbol) {
        this.declaredVariableNames.add(symbol);
    }

    public Optional<Integer> findOffsetInLexicalScope(String symbol) {
        Iterator<Function> functionScopeIter = functionStack.descendingIterator();
        while (functionScopeIter.hasNext()) {
            Function function = functionScopeIter.next();
            Optional<Integer> optionalOffset = function.getClosestOffset(symbol);
            if (optionalOffset.isPresent()) {
                return optionalOffset;
            }
        }
        return Optional.empty();
    }

    /**
     * Push a set of tags for the current tagbody.  Returns original tag names mapped to their asm label name, avoiding
     * re-use of labels for different tagbody depths.
     */
    public Map<String,String> enterTagbody(Set<String> tags) {
        int newDepth = tagToAsmLabelStack.size() + 1;
        Map<String,String> tagMap = new HashMap<>();
        for (String tag : tags) {
            String label = "tag_" + tag + "_" + newDepth;
            tagMap.put(tag, label);
        }

        tagToAsmLabelStack.push(tagMap);

        Map<String,Integer> tagToBytesAllocated = new HashMap<>();
        for (String tag : tags) {
            tagToBytesAllocated.put(tag, 0);
        }

        tagToStackBytesAllocated.push(tagToBytesAllocated);

        return tagMap;
    }

    /**
     * Pop a set of tags for the current tagbody.
     */
    public void leaveTagbody() {
        tagToAsmLabelStack.pop();
        tagToStackBytesAllocated.pop();
    }

    public Optional<String> getAsmLabelForTag(String tagName) {
        Iterator<Map<String, String>> mapIterator = tagToAsmLabelStack.descendingIterator();
        while (mapIterator.hasNext()) {
            Map<String, String> currentTagScope = mapIterator.next();
            if (currentTagScope.containsKey(tagName)) {
                return Optional.of(currentTagScope.get(tagName));
            }
        }
        return Optional.empty();
    }

    /*
     * The 'bytes' param can be negative to indicate a release of previously allocated stack has occurred.
     */
    public void recordStackAllocation(int bytes) {
        // We only need to keep count if we're currently within a tagbody (as we can encounter early exit from a structure
        // of special forms such as 'if' and we'll need to do our own stack cleanup.
        // We need to account for the allocation for all tags which we could currently jump to.  Inefficient but ok for now.
        if (!tagToStackBytesAllocated.isEmpty()) {
            for (Map<String,Integer> tagBodyTags : tagToStackBytesAllocated) {
                for (String tag : tagBodyTags.keySet()) {
                    int curr = tagBodyTags.get(tag);
                    if (curr + bytes < 0) {
                        throw new RuntimeException("Recorded stack allocation has become negative: " + tagToStackBytesAllocated);
                    }

                    tagBodyTags.put(tag, curr + bytes);
                }
            }
        }
    }

    public int getAllocatedBytesFromNearestTag(String tag) {
        Iterator<Map<String, Integer>> mapIterator = tagToStackBytesAllocated.descendingIterator();
        while (mapIterator.hasNext()) {
            Map<String, Integer> currentTagScope = mapIterator.next();
            if (currentTagScope.containsKey(tag)) {
                return currentTagScope.get(tag);
            }
        }

        return 0;
    }
}
