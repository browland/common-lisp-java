package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import function.Closure;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.stream.Collectors;

public class Funcall implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        Node closureNode = entireList.get(1);
        Value<?> closureValue = evaluator.evaluate(closureNode, environment);
        if(closureValue.getType() != ValueType.OPERATOR) {
            throw new IllegalArgumentException("Expect first arg of funcall to evaluate to a closure");
        }

        Closure closure = (Closure) closureValue.getValue();
        List<Node> argumentNodes = entireList.nodes().subList(2, entireList.nodes().size());

        List<Value<?>> operandValues = argumentNodes.stream()
                .map(argNode -> evaluator.evaluate(argNode, environment))
                .collect(Collectors.toList());

        return closure.apply(operandValues, environment);
    }

    public List<? extends Value<?>> argNodesToValues(List<Node> operands,
                                                     Evaluator evaluator,
                                                     Environment environment) {
        return operands.stream()
                .map(operand -> evaluator.evaluate(operand, environment))
                .toList();
    }
}
