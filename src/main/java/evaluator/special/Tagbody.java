package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.Symbol;
import value.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tagbody implements SpecialForm {
    private Map<Symbol,Integer> tags = new HashMap<>();

    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        Value<?> result = null;
        List<Node> nodes = entireList.nodes();
        for (int i = 0; i< nodes.size(); i++) {
            Node node = nodes.get(i);
            if(node instanceof Atom atom) {
                Symbol symbol = Symbols.internSymbol(atom.value());
                if(i < nodes.size()-1) {
                    tags.put(symbol, i+1);
                }
                // else handle tag at end of tagbody - e.g. exit
            }
            else {
                try {
                    result = evaluator.evaluate(node, environment);
                }
                catch(GoException goException) {
                    Symbol symbol = goException.getSymbol();
                    if(tags.containsKey(symbol)) {
                        // continue loop from index of goto (less one, as it'll get incremented on next loop)
                        i = tags.get(symbol)-1;
                    }
                    else {
                        throw goException;
                    }
                }
            }
        }
        return result;
    }
}
