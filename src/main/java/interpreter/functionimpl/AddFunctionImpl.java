package interpreter.functionimpl;

import parser.Form;
import parser.Node;

import java.util.List;

public class AddFunctionImpl implements FunctionImpl {
    private List<Node> operands;

    public AddFunctionImpl(List<Node> operands) {
        this.operands = operands;
    }
    @Override
    public String apply(List<Form> arguments) {
        // todo not ready
//        // assume two args
//        int argOne = Integer.parseInt(arguments.get(0).rawText());
//        int argTwo = Integer.parseInt(arguments.get(1).rawText());
//
//        return Integer.toString(argOne + argTwo);
        return null;
    }
}
