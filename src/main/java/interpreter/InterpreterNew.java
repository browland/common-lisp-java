package interpreter;

import parser.FormTreeBuilder;
import parser.Node;

import java.util.List;

public class InterpreterNew {

    static String interpret(String program) {
        List<Node> nodes = FormTreeBuilder.parse(program);
        Node functionNode = nodes.get(0);
        List<Node> arguments = nodes.subList(1, nodes.size());

        Function function  = new Function(functionNode);
        return function.apply(arguments);
    }
}
