package compiler.specialform;

import compiler.treewalker.CompilerBackend;
import compiler.treewalker.TreeWalker;
import syntaxtree.RList;

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

    @Override
    public void walkTree(RList lambdaForm, TreeWalker treeWalker, CompilerBackend backend) {
        LambdaEvaluator.SINGLETON.walkTree(lambdaForm, treeWalker, backend);
    }
}
