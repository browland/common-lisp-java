package reader;

import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.util.ArrayList;
import java.util.List;

public class NewListBuilder {
    private SimplerTokeniser tokeniser = new SimplerTokeniser();
    private RList currentList;
    private boolean quoting;

    public Node build(String program) {
        List<String> tokens = tokeniser.tokenise(program);
        for(int i = 0; i<tokens.size(); i++) {
            String token = tokens.get(i);
            boolean improperList = false;
            if(token.equals("(")) {
                // determine if the second element in this list is a single dot - if so then this is an improper list
                // and we should handle it differently
                if(tokens.size() >= i+2+1) {
                    String possibleDot = tokens.get(i+2);
                    improperList = possibleDot.equals(".");
                }
                newList(improperList);
            }
            else if(token.equals(")")) {
                closeList();
            }
            else if(token.equals("'")){
                handleQuote();
            }
            else {
                if(currentList != null) {
                    addAtom(token);
                }
                else {
                    // bare atom only
                    return new Atom(token, null);
                }
            }
        }
        return currentList;
    }

    private void handleQuote() {
        quoting = true;
        newList(false);
        currentList.add(new Atom("quote", null));
    }

    private void addAtom(String token) {
        // if we're just about to add the third element of the list and it's dot and it's an improper list then
        // no-op
        if(currentList.improperList() && token.equals(".")) {
            if(currentList.nodes().size() == 2) {
                return;
            }
        }
        currentList.add(new Atom(token, null));

        if(quoting) {
            closeList();
            quoting = false;
        }
    }

    private void closeList() {
        RList parent = currentList.getParent();
        // Don't come up out of the top-level list, otherwise we have nothing left!
        if(parent != null) {
            currentList = parent;
        }

        if(quoting) {
            quoting = false;
            closeList();
        }
    }

    private void newList(boolean improperList) {
        if(currentList == null) {
            currentList = new RList(0, null, quoting, improperList, new ArrayList<>());
        }
        else {
            RList newList = new RList(0, null, quoting, improperList, new ArrayList<>());
            newList.setParent(currentList);
            currentList.add(newList);
            currentList = newList;

        }
        quoting = false;

        if(improperList) {
            addAtom("cons");
        }
    }
}
