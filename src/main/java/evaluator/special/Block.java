package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Symbol;
import value.Value;

import java.util.List;

public class Block implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        List<Node> nodes = entireList.nodes();
        Node nameNode = nodes.get(1);

        if (!(nameNode instanceof Atom(String name))) {
            throw new IllegalArgumentException("list provided for block name");
        }

        Symbol nameSymbol = Symbols.internSymbol(name);

        Value<?> bodyEvaluation;
        try {
            bodyEvaluation = evaluateBody(environment, evaluator, nodes);
        }
        catch(ReturnFromException returnFromException) {
            Symbol thrownBlockName = returnFromException.getBlockName();
            if(thrownBlockName.equals(nameSymbol)) {
                return returnFromException.getReturnValue();
            }
            else {
                // re-throw as we may be returning from another block outside this one
                throw returnFromException;
            }
        }

        return bodyEvaluation;
    }

    private static Value<?> evaluateBody(Environment environment, Evaluator evaluator, List<Node> nodes) {
        Value<?> bodyEvaluation = null;
        for(Node bodyNode : nodes.subList(2, nodes.size())) {
            bodyEvaluation = evaluator.evaluate(bodyNode, environment);
        }
        return bodyEvaluation;
    }
}
