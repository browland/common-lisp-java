package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

public class Defvar implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        Atom nameAtom = (Atom) entireList.get(1);
        if(nameAtom.prefix() != null || nameAtom.suffix() != null) {
            throw new IllegalArgumentException("name for defvar must be a symbol: [" + nameAtom + "]");
        }

        String name = nameAtom.value();
        Node valueNode = entireList.get(2);
        Value<?> valueValue = evaluator.evaluate(valueNode, environment);

        // todo assuming global
        environment.setGlobal(name, valueValue);

        // returns the name of the variable
        return new Value<>(name, ValueType.SYMBOL);
    }
}
