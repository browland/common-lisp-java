package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.RList;
import value.*;

import java.util.List;

public class DefMacro implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {

        RList bindingsList = (RList)entireList.get(2);
        List<Atom> bindings = bindingsList.nodes().stream()
                .map(node -> (Atom)node)
                .toList();

        RList body = (RList)entireList.get(3);

        Macro macro = new Macro(environment.capture(), bindings, body);
        MacroValue macroValue = new MacroValue(macro);

        Atom nameAtom = (Atom)entireList.get(1);
        String name = nameAtom.value();
        Symbol symbol = environment.getSymbols().internSymbol(name);
        environment.setGlobal(symbol, macroValue);

        return new StringValue(name);
    }
}
