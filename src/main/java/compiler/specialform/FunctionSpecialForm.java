package compiler.specialform;

import compiler.Namespace;
import compiler.treewalker.CompilerBackend;
import compiler.treewalker.TreeWalker;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

/*
 * `function` needs to be a special form otherwise we'd evaluate the symbol and get its value slot rather than its
 * function slot.  We just return the function object from the function slot associated with this symbol.
 */
public class FunctionSpecialForm implements SpecialForm {
    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, CompilerBackend backend) {
        Node functionSymbolNode = rlist.get(1);

        // The function designator may be an atom (e.g. function stored in variable slot).
        // It may also be a list but in only one case: when a lambda expression.
        if (functionSymbolNode instanceof Atom functionAtom) {
            String functionName = functionAtom.value();

            // TODO assuming the function symbol will always be defined by the time we quote it hence not init'ing it like for
            // regular symnbols e.g. in QuoteSpecialForm; it's certainly impossible to quote an undefined function in sbcl so should be okay?
            backend.handleSymbolOperand(functionName, Namespace.FUNCTION);
        }
        else if (functionSymbolNode instanceof RList functionRList) {
            // We eval the lambda and return the closure object
            LambdaEvaluator.SINGLETON.walkTree(functionRList, treeWalker, backend);
        }
    }
}
