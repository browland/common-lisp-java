package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.IntegerValue;
import value.Symbol;
import value.Value;

/**
 * Finds the binding at the most local lexical level and set that to the new value provided.
 */
public class Incf implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {

        Atom symbolAtom = (Atom) entireList.get(1);
        String name = symbolAtom.value();
        Symbol symbol = Symbols.internSymbol(name);

        // evaluate the value currently set to the symbol (should be int)
        Value<?> value = evaluator.evaluate(symbolAtom, environment);
        IntegerValue intValue = value.expectInt("incf");
        int newVal = intValue.getValue() + 1;
        IntegerValue newIntValue = new IntegerValue(newVal);

        environment.setVariable(symbol, newIntValue);

        return newIntValue;
    }
}
