package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Symbol;
import value.Value;

public class Go implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList, Environment environment, Evaluator evaluator) {
        Node symbolNode = entireList.nodes().get(1);
        if(!(symbolNode instanceof Atom)) {
            throw new IllegalArgumentException("go should be followed by an atom");
        }

        Atom symbolAtom = (Atom)symbolNode;

        String symbolStr = symbolAtom.value();
        Symbol symbol = Symbols.internSymbol(symbolStr);

        throw new GoException(symbol);
    }
}
