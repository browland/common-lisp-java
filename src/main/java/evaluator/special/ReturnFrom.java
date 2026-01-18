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

public class ReturnFrom implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList, Environment environment, Evaluator evaluator) {
        List<Node> nodes = entireList.nodes();
        Node nameNode = nodes.get(1);
        if (!(nameNode instanceof Atom nameAtom)) {
            throw new IllegalArgumentException("list provided for return-from name");
        }

        String name = nameAtom.value();

        Value<?> returnValue;
        if (nodes.size() < 3) {
            returnValue = Value.nil();
        }
        else if(nodes.size() > 4) {
            throw new IllegalArgumentException("additional arg(s) supplied in return-from");
        }
        else {
            Node valueNode = nodes.get(2);
            returnValue = evaluator.evaluate(valueNode, environment);
        }

        Symbol blockSymbol = Symbols.internSymbol(name);
        throw new ReturnFromException(blockSymbol, returnValue);
    }
}
