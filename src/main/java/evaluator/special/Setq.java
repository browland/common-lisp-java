package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.Symbol;
import value.Value;

/**
 * Finds the binding at the most local lexical level and set that to the new value provided.
 */
public class Setq implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {

        Atom symbolAtom = (Atom) entireList.get(1);
        if((symbolAtom.prefix() != null && !symbolAtom.prefix().isEmpty())) {
            throw new IllegalArgumentException("in setq, not a symbol: [" + symbolAtom + "]");
        }

        String name = symbolAtom.value();
        Symbol symbol = Symbols.internSymbol(name);

        // evaluate the value being set to the symbol
        Value<?> value = evaluator.evaluate(entireList.nodes().get(2), environment);

        environment.setInMostLocalScope(symbol, value);

        return value;
    }
}
