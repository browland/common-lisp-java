package interpreter;

import parser.Node;
import parser.NodeType;
import parser.ParserNew;

import java.util.List;

public record Function(Node node) {

    Function ofNode(Node functionNode) {
        if(!(NodeType.FUNCTION == functionNode.type())) {
            throw new IllegalArgumentException("node is not a function! " + functionNode);
        }

        return new Function(node);
    }

    String apply(List<Node> arguments) {
        boolean canEvaluateNow = functionNeedsRecursion();
        return "";
    }

    boolean functionNeedsRecursion() {
        String text = node().text();
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
