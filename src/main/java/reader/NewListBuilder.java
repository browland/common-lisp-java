package reader;

import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.ArrayList;
import java.util.List;

public class NewListBuilder {
    private SimplerTokeniser tokeniser = new SimplerTokeniser();
    private RList currentList;

    public Node build(String program) {
        List<String> tokens = tokeniser.tokenise(program);
        for(String token : tokens) {
            if(token.equals("(")) {
                newList();
            }
            else if(token.equals(")")) {
                closeList();
            }
            else {
                if(currentList != null) {
                    currentList.add(new Atom(token, null));
                }
                else {
                    // bare atom only
                    return new Atom(token, null);
                }
            }
        }
        return currentList;
    }

    private void closeList() {
        RList parent = currentList.getParent();
        // Don't come up out of the top-level list, otherwise we have nothing left!
        if(parent != null) {
            currentList = parent;
        }
    }

    private void newList() {
        if(currentList == null) {
            currentList = new RList(0, null, false, false, new ArrayList<>());
        }
        else {
            RList newList = new RList(0, null, false, false, new ArrayList<>());
            newList.setParent(currentList);
            currentList.add(newList);
            currentList = newList;

        }
    }
}
