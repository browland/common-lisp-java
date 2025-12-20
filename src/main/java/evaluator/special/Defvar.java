package evaluator.special;

import evaluator.Evaluator;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.Map;

public class Defvar implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Map<String, Value<?>> environment,
                             Evaluator evaluator) {
        Atom nameAtom = (Atom) entireList.get(1);
        if(nameAtom.prefix() != null || nameAtom.suffix() != null) {
            throw new IllegalArgumentException("name for defvar must be a symbol: [" + nameAtom + "]");
        }

        String name = nameAtom.value();
        Node valueNode = entireList.get(2);
        Value<?> valueValue = evaluator.evaluate(valueNode, environment);

        environment.put(name, valueValue);

        // returns the name of the variable
        return new Value<>(name, ValueType.SYMBOL);
    }
}
