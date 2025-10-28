package interpreter.functionimpl;

import parser.Form;
import parser.Node;

import java.util.List;

public class LambdaFunctionImpl implements FunctionImpl {
    private final Form functionForm;  // form of the function itself; args will be supplied by apply()
                                      // this form wouldn't be able to be applied by itself; it needs argument(s)

    public LambdaFunctionImpl(Form functionForm) {
        this.functionForm = functionForm;
    }

    @Override
    public String apply(List<Node> arguments) {
        // At this point we can always successfully apply the lambda.  Any reduction has already been done.
        // At my current understanding, for now we generally just substitute the named argument with the incoming value
        // for that argument.
        // Step 1: extract the name for the argument from the function form
        // Step 2: extract the body of the lambda
        // Step 3: substitute the supplied argument value, for the name being substituted
        // Step 4: return the result

        // functionForm.nodes[0] is the only node.  It's of type FUNCTION, with rawText (lambda (x) (lambda (y) (+ x y)))
        // parentFormRawText == rawText because it's the top-level form being parsed from the test
        // children is empty
        // arguments is list of one Node(NodeType.ATOM, "10", "...", [])
        // we need to return: "(lambda (y) (+ 10 y))"
        return null;
    }
}
