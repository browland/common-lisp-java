package parser;

import interpreter.functionimpl.FunctionImpl;

import java.util.List;

public record Form(FunctionImpl functionMayBeNull,
                   String valueMayBeNull,
                   List<Form> childForms) {
    public String apply() {
        Form operator = childForms.get(0);
        Form operand = childForms.get(1);

        // todo not ready for any of this yet
//        if(!(operator.type() == NodeType.OPERATOR && operand.type() == NodeType.OPERAND)) {
//            throw new UnsupportedOperationException("need operator and operand for now");
//        }
//
//        Function function = Function.ofNode(operator);
//
//        // the actual functionality of how the operator works
//        FunctionImpl functionImpl = new LambdaFunctionImpl(operator);
//
//        function.apply(List.of(operand));

        return null;


    }
}
