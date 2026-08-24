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
    private final Deque<Function> functionStack = new LinkedList<>();
    private final Map<String,Function> functionsMap = new HashMap<>();

    public CompilerBackend() {
        functionsMap.put("add", new Function("add", Map.of()));
        functionsMap.put("+", new Function("add", Map.of()));
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

    public void handleIntOperand(IntAtom intAtom, int index) {
        System.out.printf("Storing int literal operand %d to register 0\n", intAtom.getValue(), index);
        asmGenerator.writeFixNumToRegister(0, intAtom.getFixNum());
    }

    public void startFunction(String name) {
        asmGenerator.startFunctionDef();
        initialiseSymbol(name);
        functionPrologue(name);
    }

    public void initialiseSymbol(String symbol) {
        asmGenerator.addToSymbolTable(symbol);
    }

    public void functionPrologue(String name) {
        asmGenerator.initFunction(name);
    }

    public void reserveStackForVariables(int numVariables) {
        int stackBytes = determineStackBytes(numVariables);
        System.out.printf("Reserving %d bytes of stack\n", stackBytes);
        asmGenerator.reserveSpaceOnStack(stackBytes);
    }

    public void freeStackForVariables(int numVariables) {
        int stackBytes = determineStackBytes(numVariables);
        System.out.printf("Freeing %d bytes of stack\n", stackBytes);
        asmGenerator.freeSpaceOnStack(stackBytes);
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

    public void addFunctionToSymbolTable(String name) {
        asmGenerator.putFunction(name);
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

    public void storeResultToVariable(int variableIndex) {
        System.out.printf("Storing result from x0 to stack at index %d\n", variableIndex);
        asmGenerator.storeResultToStack(variableIndex);
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

    public void createClosure(String lambdaAsmName, List<String> capturedVariables) {
        int numCaptures = Math.max(capturedVariables.size(), 8);  // ensure we don't alloc 0 byte array;
        asmGenerator.mkCaptures(numCaptures);

        // captures ptr is now in x0

        Function currentFunctionScope = functionStack.peek();
        for(int captureIndex=0; captureIndex<capturedVariables.size(); captureIndex++) {
            // Copies captured variable at frame pointer offset in current lexical scope to the next position in the heap-allocated captures array.
            String capturedVariable = capturedVariables.get(captureIndex);
            int sourceOffsetInThisLexicalScope = currentFunctionScope.getClosestOffset(capturedVariable)
                    .orElseThrow(() -> new IllegalStateException("Have captured var but can't find it in enclosing scope!"));
            System.out.println("adding capture for var %s in lexical scope at offset %d".formatted(capturedVariable, sourceOffsetInThisLexicalScope) );
            asmGenerator.addCapture(sourceOffsetInThisLexicalScope, captureIndex);
            // todo captures ptr is now in x0 again as it's returned from add_capture
        }

        // heap allocate this closure object - holds function ptr and captures array
        // This ends up being the value of this lambda evaluation.  If this seems counter-intuitive, the remaining code
        // from here switches to other `AsmContext`s in order to write the symbol table entry and asm function, out of
        // the main flow.
        asmGenerator.putClosure(lambdaAsmName);
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
        asmGenerator.storeOperandFromRegisterToStack(closurePtrRegNum, closurePtrFPOffset);

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
            asmGenerator.storeOperandFromRegisterToStack(operandNum, framePointerOffset);
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
            asmGenerator.storeOperandFromRegisterToStack(0, framePointerOffset);

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

    public void untagFunctionPointer() {
        asmGenerator.untagFunctionPtr();
    }

    public void callFunction(int functionPtrRegister) {
        System.out.printf("Calling function ptr in register %d\n", functionPtrRegister);
        asmGenerator.callFunction(functionPtrRegister);
    }

    public void loadRealFunctionPointer() {
        asmGenerator.loadRealFxnPtr();
    }

    public Function findFunction(String symbol) {
        return functionsMap.get(symbol);
    }

    private int determineStackBytes(int numVariables) {
        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        return (int)(16 * Math.ceil(numVariables/2f));
    }
}
