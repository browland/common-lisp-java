package evaluator.macro;

import function.Closure;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.ClosureValue;

import java.util.ArrayList;
import java.util.List;

public class ClosureToLambda {

    public static RList toLambdaNode(ClosureValue closureValue) {
        Closure closure = closureValue.getValue();

        List<Node> bindings = closure.bindings();
        List<Node> forms = closure.forms();
        Atom lambdaSymbol = new Atom("lambda", null);

        RList bindingsRList = new RList(false, bindings, false);
        RList formsRList = new RList(false, forms, false);

        List<Node> topLevelNodes = new ArrayList<>();
        topLevelNodes.add(lambdaSymbol);
        topLevelNodes.add(bindingsRList);
        topLevelNodes.addAll(formsRList.nodes());

        return new RList(false, topLevelNodes, false);
    }
}
