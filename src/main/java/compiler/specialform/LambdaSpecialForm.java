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
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator, Function currentFunctionScope) {
        // Step 1. Create the closure object.
        // All we need so far is:
        // 1. Function pointer (based on name of lambda function within the asm)
        // 2. Captures array (created on heap; we'll generate code to copy the values of the free variables this lambda
        //    depends upon into this array
        // 3. Create a closure object on heap - this contains the two pointers to the function and the captures array.
        //
        // The closure pointer will be tagged, and the function pointer within will be tagged.
        String lambdaFunctionName = "closure_" + num++;

        // TODO we need to assess bindings and do escape analysis here otherwise we don't know how many captures

        asmGenerator.mkCaptures(1);
        asmGenerator.addCapture(-16);  // TODO fake code to copy x as a capture; we know it's at [fp, -16] here in the flow from debugging

        // heap allocate this closure object - holds function ptr and captures array
        // This ends up being the value of this lambda evaluation.  If this seems counter-intuitive, the remaining code
        // from here switches to other `AsmContext`s in order to write the symbol table entry and asm function, out of
        // the main flow.
        asmGenerator.putClosure(lambdaFunctionName);

        // TODO actually handle/use offsets
        Map<String,Integer> captureOffsets = Map.of("x", 0);
        generateLambdaFunctionImpl(asmGenerator, lambdaFunctionName, rlist, treeWalker, captureOffsets);
    }

    // ****************************************
    // *** Code gen for lambda application time
    // ****************************************
    private void generateLambdaFunctionImpl(AsmGenerator asmGenerator, String lambdaFunctionName, RList lambdaForm, TreeWalker treeWalker, Map<String, Integer> captureOffsets) {
        // Write symbol table entry for the closure and its function slot in the symbol table.
        asmGenerator.addToSymbolTable(lambdaFunctionName);

        // put the AsmGenerator into new function scope
        asmGenerator.startFunctionDef();
        asmGenerator.initFunction(lambdaFunctionName);

        // Determine bindings
        Node bindingsNode = lambdaForm.nodes().get(1);
        List<Node> bindingsList = RList.expectRList(bindingsNode).nodes();
        int numBindings = bindingsList.size();
        int numCaptures = captureOffsets.size();

        // We need 16 bytes for each binding, but ensure we always reserve a multiple of 16 bytes
        final int stackBytes = (int)(16 * Math.ceil((numBindings + numCaptures)/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        int pos = 0;
        Map<String, Integer> stackOffsets = new HashMap<>();
        for (Node bindingNode : bindingsList) {
            Atom symbolAtom = Atom.expectAtom(bindingNode);
            System.out.printf("lambda: storing binding for %s to stack%n", symbolAtom.value());
            asmGenerator.storeOperandFromRegisterToStack(pos);
            // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
            int stackOffset = -1*stackBytes + (pos*8);
            stackOffsets.put(symbolAtom.value(), stackOffset);
            pos++;
        }

        // TODO temp one-off capture handling
        //      expect one more argument than bindings, which is captures ptr
        System.out.printf("lambda: storing capture for %s to stack%n", "x");
        asmGenerator.loadCapturedVariable(0);  // TODO hardcoded with 0th captured var
        asmGenerator.storeOperandFromRegisterToStack(pos);
        // Stack offset is relative to the frame pointer and starting from low value; values will be e.g. {-16, -8, ...}.
        // TODO garbage as we can't rely on these stackOffsets later on anyway, it's the captures offsets we need to store here isn't it?
        //      But we need something in offsets otherwise it blows up later by looking for symbol table entry for x
        int stackOffset = -1*stackBytes + (pos*8);
        stackOffsets.put("x", stackOffset);

        // TODO do escape analysis
        //      currentFunctionScope has offsets of vars we may access in the lambda body.  If so, we need to copy
        //      these to heap and put ptr onto our stack and keep track of it in our stackOffsets
        //      For now let's cheat and hardcode it to our 'x' symbol.  This will break until we do proper tree-walking
        //      escape analysis.

        // Store stack offsets on Function so we can pass them around with relevant context
        Function function = new Function(lambdaFunctionName, stackOffsets);

        // Function impl
        Node bodyNode = lambdaForm.nodes().get(2);
        treeWalker.walkTree(bodyNode, function);

        // We need to free an additional 16 bytes for the stored x29 and x30 regs
        asmGenerator.freeSpaceOnStack(stackBytes + 16);

        asmGenerator.endFunction();

        // return the AsmGenerator from new function scope
        // TODO confusing how this differs from endFunction() - doing different things
        asmGenerator.endFunctionDef();

        // result of evaluating a lambda should be its value
        // no longer wanted; already done in putClosure()
//        asmGenerator.loadFunctionPtrResult(name);

        // add this function to our compile-time Map
        treeWalker.getFunctions().put(lambdaFunctionName, function);
    }
}
