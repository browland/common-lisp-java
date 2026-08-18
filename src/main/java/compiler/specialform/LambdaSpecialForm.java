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

        Map<String,Integer> captureOffsets = generateCaptureOffsets(currentFunctionScope, bindingsList, lambdaForm);

        asmGenerator.mkCaptures(captureOffsets.size());

        // Copies captured variable at frame pointer offset in current lexical scope to the next position in the heap-allocated captures array.
        // TODO We add captures in order of offset in captureOffsets, but reference their source offset from THIS stack frame.
        asmGenerator.addCapture(-16);  // TODO fake code to copy x as a capture; we know it's at [fp, -16] here in the flow from debugging

        // heap allocate this closure object - holds function ptr and captures array
        // This ends up being the value of this lambda evaluation.  If this seems counter-intuitive, the remaining code
        // from here switches to other `AsmContext`s in order to write the symbol table entry and asm function, out of
        // the main flow.
        asmGenerator.putClosure(lambdaFunctionName);

        generateLambdaFunctionImpl(asmGenerator, lambdaFunctionName, lambdaForm, treeWalker, captureOffsets, bindingsList);
    }

    Map<String,Integer> generateCaptureOffsets(Function currentFunctionScope, List<Node> bindingsList, RList lambdaBody) {
        // TODO actually handle/use offsets.  Collect unbound symbols in lambda body.  For each, look at currentFunctionScope.stackOffsetStack
        //      and its framePointerOffset is recorded there.
        Map<String,Integer> captureOffsets = Map.of("x", 0);
        return captureOffsets;
    }

    // ****************************************
    // *** Code gen for lambda application time
    // ****************************************
    private void generateLambdaFunctionImpl(AsmGenerator asmGenerator, String lambdaFunctionName, RList lambdaForm,
                                            TreeWalker treeWalker, Map<String, Integer> captureOffsets, List<Node> bindingsList) {
        // Write symbol table entry for the closure and its function slot in the symbol table.
        asmGenerator.addToSymbolTable(lambdaFunctionName);

        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();
        asmGenerator.initFunction(lambdaFunctionName);

        int numBindings = bindingsList.size();
        int numCaptures = captureOffsets.size();

        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        final int stackBytes = (int)(16 * Math.ceil((numBindings + numCaptures)/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        int pos = 0;
        Map<String, Integer> closureFunctionStackOffsets = new HashMap<>();
        int stackOffset;
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            System.out.printf("lambda: storing binding for %s to stack%n", symbolAtom.value());
            asmGenerator.storeOperandFromRegisterToStack(pos);
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            stackOffset = -1*stackBytes + (pos*8);
            closureFunctionStackOffsets.put(symbolAtom.value(), stackOffset);
            pos++;
        }

        // TODO temp one-off capture handling
        //      expect one more argument than bindings, which is captures ptr.  We will always set one of these.
        System.out.printf("lambda: storing capture for %s to stack%n", "x");
        asmGenerator.loadCapturedVariable(0);  // TODO hardcoded with 0th captured var
        asmGenerator.storeOperandFromRegisterToStack(pos);

        // continue with next stack offset for captured variable; this has a bit of a smell perhaps.  We work out the
        // stack pos within storeOperandFromRegisterToStack() relative to the stack pointer, but repeat the calculation
        // here relative to the frame pointer (as it's stable over time).
        stackOffset = -1*stackBytes + (pos*8);
        closureFunctionStackOffsets.put("x", stackOffset);

        // Store stack offsets on Function; needed in walkTree() to generate instructions for entirety of lambda body.
        Function function = new Function(lambdaFunctionName, closureFunctionStackOffsets);

        // Function impl
        Node bodyNode = lambdaForm.nodes().get(2);
        treeWalker.walkTree(bodyNode, function);

        // We need to free an additional 16 bytes for the stored x29 and x30 regs
        asmGenerator.freeSpaceOnStack(stackBytes + 16);

        asmGenerator.endFunction();

        // return the AsmGenerator from new function scope
        // TODO confusing how this differs from endFunction() - doing different things
        asmGenerator.endFunctionDef();

        // add this function to our compile-time Map; needed for compile time logic when we need to look it up at apply time.
        treeWalker.getFunctions().put(lambdaFunctionName, function);
    }
}
