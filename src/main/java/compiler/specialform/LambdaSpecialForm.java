package compiler.specialform;

import compiler.treewalker.CompilerBackend;
import compiler.treewalker.EscapeAnalyser;
import compiler.treewalker.Function;
import compiler.treewalker.TreeWalker;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.List;
import java.util.Set;

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
    private EscapeAnalyser escapeAnalyser = new EscapeAnalyser();
    private static int num = 0;

    @Override
    public void walkTree(RList lambdaForm, TreeWalker treeWalker, CompilerBackend backend) {
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

        List<String> capturedVariables = generateClosure(lambdaFunctionName, bindingsList, lambdaForm, backend);

        generateLambdaFunctionImpl(backend, lambdaFunctionName, lambdaForm, treeWalker, capturedVariables, bindingsList);
    }

    List<String> generateClosure(String lambdaAsmName, List<Node> bindingsList, RList lambdaForm, CompilerBackend backend) {
        List<String> bindingNames = bindingsList.stream().map(bindingNode ->
        {
            Atom bindingAtom = Atom.expectAtom(bindingNode);
            return bindingAtom.value();
        }).toList();

        RList lambdaBody = RList.expectRList(lambdaForm.nodes().get(2));
        Set<String> declaredFunctionNames = backend.getDeclaredFunctionNames();
        List<String> capturedVariables = escapeAnalyser.findFreeVariables(bindingNames, declaredFunctionNames, lambdaBody);

        backend.createClosure(lambdaAsmName, capturedVariables);

        return capturedVariables;
    }

    // ****************************************
    // *** Code gen for lambda application time
    // ****************************************
    private void generateLambdaFunctionImpl(CompilerBackend backend, String lambdaFunctionName, RList lambdaForm,
                                            TreeWalker treeWalker, List<String> capturedVariables, List<Node> bindingsList) {

        backend.startFunction(lambdaFunctionName, true);

        Function function = backend.setUpClosureFunctionStack(capturedVariables, bindingsList, lambdaFunctionName);

        // Function impl
        Node bodyNode = lambdaForm.nodes().get(2);
        treeWalker.walkTree(bodyNode);

        backend.endFunction(function);
    }
}
