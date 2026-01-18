package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import function.Closure;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

public class Function implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // expect single arg representing the function - we return it as a value (a closure).
        Node functionNode = entireList.nodes().get(1);

        Value<?> closureValue = evaluator.evaluate(functionNode, environment);
        if(closureValue.getType() != ValueType.OPERATOR) {
            throw new IllegalArgumentException("arg to 'function' can't be evaluated as a closure");
        }

        return closureValue;
    }
}
