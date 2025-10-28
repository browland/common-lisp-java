package interpreter.functionimpl;

import parser.Node;

import java.util.List;

public interface FunctionImpl {
    public String apply(List<Node> arguments);
}
