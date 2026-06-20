package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import exception.EvaluationException;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.*;

import java.util.List;

public class DefMacro implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {

        if(entireList.size() < 4) {
            throw new EvaluationException("too few elements for defmacro");
        }

        RList bindingsList = (RList)entireList.get(2);
        List<Node> bindings = bindingsList.nodes();

        List<Node> allNodes = entireList.nodes();
        List<Node> bodyNodes = allNodes.subList(3, allNodes.size());

        Atom nameAtom = (Atom)entireList.get(1);
        String name = nameAtom.value();
        Symbol symbol = Symbols.internSymbol(name);

        Macro macro = new Macro(bindings, bodyNodes, name);
        MacroValue macroValue = new MacroValue(macro);

        environment.setMacro(symbol, macroValue);

        return new StringValue(name);
    }
}
