package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import exception.EvaluationException;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.IntegerValue;
import value.Symbol;
import value.Value;

public class Incf implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList, Environment environment, Evaluator evaluator) {
        Node operandNode = entireList.get(1);

        if(!(operandNode instanceof Atom)) {
            throw new EvaluationException("incf requires an atom operand");
        }
        Atom operandAtom = (Atom)operandNode;
        String symbolString = operandAtom.value();

        Symbol symbol = Symbols.internSymbol(symbolString);
        Value<?> currValue = environment.getVariable(symbol)
                .orElseThrow(() -> new EvaluationException("incf: symbol not found: %s".formatted(symbol)));

        if(currValue instanceof IntegerValue currentIntegerValue) {
            int currValueInt = currentIntegerValue.getValue();
            IntegerValue newValue = new IntegerValue(currValueInt + 1);
            environment.setVariable(symbol, newValue);
            return newValue;
        }
        else {
            throw new EvaluationException("incf: not an integer stored at symbol %s".formatted(symbol));
        }

    }
}
