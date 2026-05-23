package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Symbol;
import value.SymbolValue;
import value.Value;

public class Defvar implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        Atom nameAtom = (Atom) entireList.get(1);
        String name = nameAtom.value();
        Symbol symbol = Symbols.internSymbol(name);

        if(symbol.isKeyword()) {
            throw new RuntimeException("Can't assign a keyword symbol for " + name);
        }

        Node valueNode = entireList.get(2);
        Value<?> valueValue = evaluator.evaluate(valueNode, environment);

        environment.setVariable(symbol, valueValue);

        // returns the name of the variable
        return new SymbolValue(symbol);
    }
}
