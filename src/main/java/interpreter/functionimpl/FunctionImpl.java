package interpreter.functionimpl;

import parser.Form;

import java.util.List;

public interface FunctionImpl {
    String apply(List<Form> arguments);
}
