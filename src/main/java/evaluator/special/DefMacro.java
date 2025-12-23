package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.Macro;
import value.Value;
import value.ValueType;

import java.util.List;
import java.util.Map;

public class DefMacro implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        Atom nameAtom = (Atom)entireList.get(1);
        String name = nameAtom.value();

        RList bindingsList = (RList)entireList.get(2);
        List<Atom> bindings = bindingsList.nodes().stream()
                .map(node -> (Atom)node)
                .toList();

        RList body = (RList)entireList.get(3);

        Macro macro = new Macro(environment.capture(), bindings, body);
        Value<Macro> macroValue = new Value<>(macro, ValueType.MACRO);
        environment.setGlobal(name, macroValue);

        return new Value<>(name, ValueType.STRING_LITERAL);
    }
}
