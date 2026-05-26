package evaluator.special;

import evaluator.Evaluator;
import evaluator.OperatorLookup;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.ClosureValue;
import value.FunctionValue;
import value.Value;

public class Function implements SpecialForm {
    private OperatorLookup operatorLookup = new OperatorLookup();

    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // expect single arg representing the function - we return it as a value (a closure).
        Node functionNode = entireList.nodes().get(1);

        if(functionNode instanceof Atom atom) {
            var optionalFunction = operatorLookup.lookupFunction(atom.value(), environment);
            if(optionalFunction.isPresent()) {
                return new FunctionValue(optionalFunction.get());
            }
            else {
                throw new UnsupportedOperationException("no function  " + atom.value() + " found - illegal function call");
            }
        }
        else if(functionNode instanceof RList rList) {
            // assume it's a lambda
            Value<?> evaluatedOperator = evaluator.evaluate(functionNode, environment);
            if(evaluatedOperator instanceof ClosureValue) {
                return evaluatedOperator;
            }
            else {
                throw new UnsupportedOperationException("form passed to 'function': " + rList + " but not a lambda form");
            }
        }
        else {
            throw new UnsupportedOperationException("unhandled node passed to 'function': " + functionNode);

        }
    }
}
