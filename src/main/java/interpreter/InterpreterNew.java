package interpreter;

import parser.Node;
import parser.ParseResult;
import parser.ParserNew;

import java.util.List;

public class InterpreterNew {

    static String interpret(String program) {
        ParseResult parseResult = ParserNew.parse(program);

        List<Node> nodes = parseResult.form().nodes();
        Node functionNode = nodes.get(0);
        List<Node> arguments = nodes.subList(1, nodes.size());

        Function function  = new Function(functionNode);
        return function.apply(arguments);
    }
}
