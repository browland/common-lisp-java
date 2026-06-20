package evaluator.special;

import evaluator.AtomEvaluator;
import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.ConsCellValueFactory;
import value.Value;

public class Quote implements SpecialForm {
    private final AtomEvaluator atomEvaluator = new AtomEvaluator();

    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // the single operand is either a list or an atom
        Node operand = entireList.nodes().get(1);

        if(operand instanceof Atom atom) {
            return atomEvaluator.atomToValueNoLookup(atom.value());
        }
        else if(operand instanceof RList rlist) {
            return ConsCellValueFactory.fromRList(rlist);
        }
        else {
            throw new UnsupportedOperationException("Unhandled type for quote " + operand);
        }
    }
}
