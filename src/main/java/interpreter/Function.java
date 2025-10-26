package interpreter;

import parser.Node;
import parser.NodeType;
import parser.ParseResult;
import parser.ParserNew;

import java.util.List;

public record Function(Node node) {

    static Function ofNode(Node functionNode) {
        if(!(NodeType.FUNCTION == functionNode.type())) {
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
            String textWithSubstitutions = node().formRawText().replace(childFunctionNode.rawText(), childResult);

            return InterpreterNew.interpret(textWithSubstitutions);
        }

        // simpler case where we don't need to recursively evaluate the function
        return "";
    }

    // todo reversed logic?
    boolean functionNeedsRecursion() {
        String text = node().rawText();
        boolean embeddedFunction = ParserNew.functionIsEmbedded(text);
        if(!embeddedFunction) {
            return true;
        }

        int depth = 0;
        for(int i = 0; i< text.length(); i++) {
            char c = text.charAt(i);
            if(c == '(') {
                depth++;
                if(depth > 1) {
                    return false;
                }
            }
            else if(c == ' ') {
                continue;  // spaces are ignored for this
            }
            else {
                return true;  // only open-brackets can make a difference
            }
        }
        throw new IllegalStateException("can't get here");
    }
}
