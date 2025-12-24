package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.ConsCellValue;
import value.Value;

public class Quote implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // the single operand is either a list or an atom
        Node operand = entireList.nodes().get(1);

        if(operand instanceof Atom atom) {
            return Value.of(atom.value());
        }
        else if(operand instanceof RList rlist) {
            return ConsCellValue.fromJavaList(rlist);
        }
        else {
            throw new UnsupportedOperationException("Unhandled type for quote " + operand);
        }
    }
}
