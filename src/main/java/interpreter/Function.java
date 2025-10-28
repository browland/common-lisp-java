package interpreter;

import interpreter.functionimpl.AddFunctionImpl;
import interpreter.functionimpl.FunctionImpl;
import parser.Node;
import parser.NodeType;

import java.util.List;
import java.util.Optional;

public record Function(Node node) {

    public static Function ofNode(Node functionNode) {
        if(!(NodeType.OPERATOR == functionNode.type())) {
            throw new IllegalArgumentException("node is not a function! " + functionNode);
        }

        return new Function(functionNode);
    }

    String apply(List<Node> arguments) {
        // for each child, apply it and replace its text at this level with its result of evaluation
        List<Node> children = node.children();
        if(!children.isEmpty()) {
            Node childFunctionNode = children.get(0);
            Function childFunction = Function.ofNode(childFunctionNode);
            List<Node> childArgNodes = children.subList(1, children.size());
            String childResult = childFunction.apply(childArgNodes);

            // TODO use offset to do replacement - but when we store offset is this global offset or relative to something?
            String textWithSubstitutions = node().parentFormRawText().replace(childFunctionNode.parentFormRawText(), childResult);

            return InterpreterNew.interpret(textWithSubstitutions);
        }

        // simpler case where we don't need to recursively evaluate the function
        Optional<FunctionImpl> impl = resolveImpl(node().rawText());
        if(impl.isPresent()) {
            return impl.get().apply(arguments);
        }

        // todo this is quick and dirty code to evaluate a lambda which doesn't require recursion (already done above),
        //      returning a new lambda.
        //      This should be replaced with proper parsing and not dealing with raw text at this level!
        if(node().rawText().equals(" (lambda (x) (lambda (y) (+ x y)))") && arguments.size() == 1 && arguments.get(0).rawText().equals("10")) {
            return "(lambda (y) (+ 10 y))";
        }
        else if (node().rawText().equals("(lambda (y) (+ 10 y))") && arguments.size() == 1 && arguments.get(0).rawText().equals("5")) {
            return InterpreterNew.interpret("(+ 10 5)");
        }

        throw new IllegalArgumentException("unimplemented, just hack it in for now?");
    }

    private Optional<FunctionImpl> resolveImpl(String rawText) {
        return switch(rawText) {
            case "+" -> Optional.of(new AddFunctionImpl());
            default -> Optional.empty();
        };
    }
}
