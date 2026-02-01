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
    private final Map<Symbol,Integer> tags = new HashMap<>();

    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        List<Node> nodes = entireList.nodes();
        List<Node> operandNodes = nodes.subList(1, nodes.size());

        // need to do an initial walk of the nodes to collect the tags and indices, so we can jump forward if needed.
        for (int i = 0; i< operandNodes.size(); i++) {
            Node node = operandNodes.get(i);
            if(node instanceof Atom atom) {
                Symbol symbol = Symbols.internSymbol(atom.value());
                if(i < operandNodes.size()-1) {
                    tags.put(symbol, i+1);
                }
                else {
                    // We treat index -1 as returning the value (essentially jump straight to end)
                    tags.put(symbol, -1);
                }
            }
        }

        Value<?> result = Value.nil();
        for (int i = 0; i< operandNodes.size(); i++) {
            Node node = operandNodes.get(i);

            // Skip the tags; we already collected them
            if(node instanceof Atom) {
                continue;
            }

            try {
                result = evaluator.evaluate(node, environment);
            } catch (GoException goException) {
                Symbol symbol = goException.getSymbol();
                if (tags.containsKey(symbol)) {
                    int jumpIndex = tags.get(symbol);
                    if(jumpIndex == -1) {
                        // essentially goto end
                        return result;
                    }

                    // continue loop from index of goto (less one, as i will get incremented on next loop)
                    i = jumpIndex - 1;
                } else {
                    throw goException;
                }
            }
        }
        return result;
    }
}
