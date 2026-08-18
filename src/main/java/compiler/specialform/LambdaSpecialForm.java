package compiler.specialform;

import compiler.AsmGenerator;
import compiler.treewalker.Function;
import compiler.treewalker.TreeWalker;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Since this area can be confusing, it's important to think that this class is responsible for evaluating a lambda
 * function when encountered in the tree.  We're not *applying* it yet.
 * Essentially, we just create an asm function containing the instructions to implement the lambda body and don't
 * generate code to apply it here.
 * However, the instructions we generate here will result in the closure pointer (value) sitting in x0, ready for the
 * next operation in the tree.  E.g. maybe the parent node in the tree will directly apply it.  Or maybe we're generating
 * a function as a value for something like `mapcar`.  Or adding closures into a cons list; whatever.  Important to
 * bear in mind we just create a closure value (pointer in x0, backed by the asm function and captures array on the heap).
 */
public class LambdaSpecialForm implements SpecialForm {
    private static int num = 0;

    @Override
    public void walkTree(RList lambdaForm, TreeWalker treeWalker, AsmGenerator asmGenerator, Function currentFunctionScope) {
        // Step 1. Create the closure object.
        // All we need so far is:
        // 1. Function pointer (based on name of lambda function within the asm)
        // 2. Captures array (created on heap; we'll generate code to copy the values of the free variables this lambda
        //    depends upon into this array
        // 3. Create a closure object on heap - this contains the two pointers to the function and the captures array.
        //
        // The closure pointer will be tagged, and the function pointer within will be tagged.
        String lambdaFunctionName = "closure_" + num++;

        // Determine bindings
        Node bindingsNode = lambdaForm.nodes().get(1);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();

        List<String> capturedVariables = generateCaptures(currentFunctionScope, bindingsList, lambdaForm);
        asmGenerator.mkCaptures(capturedVariables.size());

        for(int captureIndex=0; captureIndex<capturedVariables.size(); captureIndex++) {
            // Copies captured variable at frame pointer offset in current lexical scope to the next position in the heap-allocated captures array.
            String capturedVariable = capturedVariables.get(captureIndex);
            int sourceOffsetInThisLexicalScope = currentFunctionScope.getClosestOffset(capturedVariable)
                    .orElseThrow(() -> new IllegalStateException("Have captured var but can't find it in enclosing scope!"));
            asmGenerator.addCapture(sourceOffsetInThisLexicalScope);
        }

        // heap allocate this closure object - holds function ptr and captures array
        // This ends up being the value of this lambda evaluation.  If this seems counter-intuitive, the remaining code
        // from here switches to other `AsmContext`s in order to write the symbol table entry and asm function, out of
        // the main flow.
        asmGenerator.putClosure(lambdaFunctionName);

        generateLambdaFunctionImpl(asmGenerator, lambdaFunctionName, lambdaForm, treeWalker, capturedVariables, bindingsList);
    }

    List<String> generateCaptures(Function currentFunctionScope, List<Node> bindingsList, RList lambdaBody) {
        // TODO for now we'll just look one level deep for free variables.  Really, we want an alternative TreeWalker impl which can do escape analysis at arbitrary depth!
        List<String> capturedVars = List.of("x");
        return capturedVars;
    }

    // ****************************************
    // *** Code gen for lambda application time
    // ****************************************
    private void generateLambdaFunctionImpl(AsmGenerator asmGenerator, String lambdaFunctionName, RList lambdaForm,
                                            TreeWalker treeWalker, List<String> capturedVariables, List<Node> bindingsList) {
        // Write symbol table entry for the closure and its function slot in the symbol table.
        asmGenerator.addToSymbolTable(lambdaFunctionName);

        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();

        Function function = setUpClosureFunctionStack(asmGenerator, capturedVariables, bindingsList, lambdaFunctionName);

        // Function impl
        Node bodyNode = lambdaForm.nodes().get(2);
        treeWalker.walkTree(bodyNode, function);

        // We need to free an additional 16 bytes for the stored x29 and x30 regs
        int stackBytes = function.getStackBytes() + 16;
        System.out.println("lambda: freeing stack bytes: %d".formatted(stackBytes));
        asmGenerator.freeSpaceOnStack(stackBytes + 16);

        asmGenerator.endFunction();

        // return the AsmGenerator from new function scope
        // TODO confusing how this differs from endFunction() - doing different things
        asmGenerator.endFunctionDef();

        // add this function to our compile-time Map; needed for compile time logic when we need to look it up at apply time.
        treeWalker.getFunctions().put(lambdaFunctionName, function);
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
    private Function setUpClosureFunctionStack(AsmGenerator asmGenerator, List<String> capturedVariables,
                                                 List<Node> bindingsList, String lambdaFunctionName) {
        // Usual save of FP/LR
        asmGenerator.initFunction(lambdaFunctionName);

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

        int stackPos = 1;  // Our next stack slot is 1 higher than the closure ptr we just stored
        Map<String, Integer> closureFunctionFPOffsets = new HashMap<>();
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

            System.out.printf("lambda: loading captures ptr from stack at FP offset %d%n", closurePtrFPOffset);
            asmGenerator.loadCapturedVariable(captureIndex, closurePtrFPOffset);
            System.out.printf("lambda: storing capture for %s result from x0 to stack at FP offset %d%n", capturedVariable, framePointerOffset);
            asmGenerator.storeOperandFromRegisterToStack(0, framePointerOffset);

            closureFunctionFPOffsets.put(capturedVariable, framePointerOffset);

            stackPos++;
        }

        // Store stack offsets on Function; needed in walkTree() to generate instructions for entirety of lambda body.
        return new Function(lambdaFunctionName, closureFunctionFPOffsets);
    }
}
