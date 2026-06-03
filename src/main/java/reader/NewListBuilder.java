package reader;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NewListBuilder {
    private SimplerTokeniser tokeniser = new SimplerTokeniser();
    private RList currentList;

    public static void main(String[] args) throws IOException {
        String program = Files.readString(Path.of("/Users/ben/git/lisp/lisp-sources/adventure.lisp"));
        NewListBuilder builder = new NewListBuilder();
        Node rootNode = builder.build(program);

        Evaluator evaluator = new Evaluator();
        evaluator.evaluate(rootNode, new Environment());
    }

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
            else if(token.startsWith("#'")){
                handleFunctionQuote();
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

    private void handleFunctionQuote() {
        newList(false);
        currentList.add(new Atom("function", null));
    }

    private void handleQuote() {
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

        if(currentList.get(0) instanceof Atom possibleQuoteAtom) {
            if(possibleQuoteAtom.value().equals("quote")) {
                closeList();
            }
        }
    }

    private void closeList() {
        RList parent = currentList.getParent();
        // Don't come up out of the top-level list, otherwise we have nothing left!
        if(parent != null) {
            currentList = parent;
            if(currentList.get(0) instanceof Atom possibleQuoteAtom) {
                if(possibleQuoteAtom.value().equals("quote")) {
                    closeList();
                }
            }
        }
    }

    private void newList(boolean improperList) {
        if(currentList == null) {
            currentList = new RList(0, null, false, improperList, new ArrayList<>());
        }
        else {
            RList newList = new RList(0, null, false, improperList, new ArrayList<>());
            newList.setParent(currentList);
            currentList.add(newList);
            currentList = newList;

        }

        if(improperList) {
            addAtom("cons");
        }
    }
}
