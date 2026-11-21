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
 * See comments on LambdaSpecialForm.
 * Common code for both the lambda special form and also the function special form (when a lambda is supplied).
 */
public class LambdaEvaluator {
    private EscapeAnalyser escapeAnalyser = new EscapeAnalyser();
    private static int num = 0;
    // To ensure only one sequence of closure function indexing (due to num field above)
    static LambdaEvaluator SINGLETON = new LambdaEvaluator();

    private LambdaEvaluator() {
        // Use SINGLETON instance
    }

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
        Set<String> declaredVariableNames = backend.getDeclaredVariableNames();
        Set<String> declaredSpecialForms = backend.getSpecialFormNames();
        Set<String> declaredMacroNames = backend.getMacroNames();
        List<String> capturedVariables = escapeAnalyser.findFreeVariables(bindingNames, declaredFunctionNames, declaredSpecialForms, declaredMacroNames, declaredVariableNames, lambdaBody, backend);

        backend.createClosure(lambdaAsmName, capturedVariables, bindingNames);

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
        for (Node bodyNode : lambdaForm.nodes().subList(2, lambdaForm.size())) {
            treeWalker.walkTree(bodyNode);
        }

        backend.endFunction(function);
    }
}
