package parser;

import interpreter.functionimpl.AddFunctionImpl;
import interpreter.functionimpl.FunctionImpl;
import interpreter.functionimpl.LambdaFunctionImpl;

import java.util.ArrayList;
import java.util.List;

public class FormBuilder {

    /**
     * What we do is essentially parse the nodes into a form which can be very easily applied (may be done at a later
     * stage once additional bindings etc are ready).
     * <p>
     * In order to get anything done, we must assume that a form could be a single value.  This is because the operands
     * might themselves be forms, and eventually they resolve down to a form which is a single value - the result of
     * evaluating the form representing the operand.
     * <p>
     * Otherwise we'll assume a form is comprised of an operator and operand(s).
     */
    public static Form build(List<Node> nodes) {
        // case for single value - maybe an atom and maybe something which needs evaluation
        if (nodes.size() == 1) {
            Node node = nodes.get(0);
            List<Node> children = node.children();
            if (children != null && children.size() > 0) {
                // we need to recurse to evaluate this node.  I think it's ok to "throw away" info at this level as
                // the children are the full expression of this node in more granularity ... I think ...
                return build(nodes);
            } else {
                // just an atom
                // todo hacking value in for now
                return new Form(null, node.rawText(), List.of());
            }
        }

        // case for operator and operands ...
        Node operator = nodes.get(0);
        List<Node> operands = nodes.subList(1, nodes.size());

        if (operator.type() != NodeType.OPERATOR) {
            throw new UnsupportedOperationException("need operator as first node in form for now");
        }

        for (Node operandNode : operands) {
            if (operandNode.type() != NodeType.OPERAND) {
                throw new UnsupportedOperationException("remaining nodes must be operands");
            }
        }

        // todo e.g. we need to detect the case there are children of the operator and we have to recurse
        //      This might require e.g. storing the depth on a Node so we can quickly check it's got further depth or not

        // todo be lazy and assume it's a lambda, AND it's being applied now in this form, AND no recursion needed
        // so we know the operator node is in a particular layout, and the operand is just the binding value
        // But we'll need to add complexity here to figure out what to do.
        // todo this is what we do when we no longer need to recurse

        // if we need to recurse the operator then do so
        FunctionImpl functionImpl = null;
        if(!operator.children().isEmpty()) {
            // todo ... not implemented ...
            throw new UnsupportedOperationException("not yet");
        }

        // operator has no child nodes - simple lambda
        // todo but we do need to parse it into parts.  E.g.:
        //      (lambda (x) (+ x 1))
        //      should be ...
        // todo awful
        if (operator.rawText().contains("lambda")) {
            functionImpl = new LambdaFunctionImpl.Builder(operator).build();
        } else if (operator.rawText().contains("+")) {
            functionImpl = new AddFunctionImpl(operands);
        }

        // todo we now evaluate each operand Node as a Form recursively
        //      we recurse per operand rather than passing all operands in one go to avoid the confusing case of
        //      building a form made of only operands.  Or maybe this will eventually be ok ...
        List<Form> operandForms = new ArrayList<>();
        for (Node operand : operands) {
            operandForms.add(build(List.of(operand)));
        }

        // todo just hacking FunctionImpl into Form for now - how should it be done?
        return new Form(functionImpl, null, operandForms);
    }
}
