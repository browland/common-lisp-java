package evaluator.special;

import evaluator.Evaluator;
import function.Defvar;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

public class DefvarForm implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Map<String, Value<?>> environment,
                             Evaluator evaluator) {
        // todo inline this - no point it being a function
        Defvar defvar = new Defvar();

        Atom nameAtom = (Atom) entireList.get(1);
        if(nameAtom.prefix() != null || nameAtom.suffix() != null) {
            throw new IllegalArgumentException("name for defvar must be a symbol: [" + nameAtom + "]");
        }

        String name = nameAtom.value();
        Value<?> nameValue = new Value<>(name, ValueType.SYMBOL);
        Node valueNode = entireList.get(2);
        Value<?> valueValue = evaluator.evaluateOperand(valueNode, environment);

        return defvar.apply(List.of(nameValue, valueValue), environment);
    }
}
