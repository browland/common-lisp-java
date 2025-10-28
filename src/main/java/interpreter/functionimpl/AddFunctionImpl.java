package interpreter.functionimpl;

import parser.Node;

import java.util.List;

public class AddFunctionImpl implements FunctionImpl {
    @Override
    public String apply(List<Node> arguments) {
        // assume two args
        int argOne = Integer.parseInt(arguments.get(0).rawText());
        int argTwo = Integer.parseInt(arguments.get(1).rawText());

        return Integer.toString(argOne + argTwo);
    }
}
