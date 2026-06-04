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
import java.util.Set;

public class NewListBuilder {
    private static final Set<String> TOKENS_WHICH_INSERT_AS_LIST = Set.of("quote", "unquote", "quasiquote", "unquote-splicing", "function");

    private SimplerTokeniser tokeniser = new SimplerTokeniser();
    private RList currentList;

    public static void main(String[] args) throws IOException {
        // todo need to handle quasiquote and unquote and unquote-unsplicing
        String program = Files.readString(Path.of("/Users/ben/git/lisp/lisp-sources/fibonacci.lisp"));
        NewListBuilder builder = new NewListBuilder();
        List<Node> forms = builder.build(program);

        Evaluator evaluator = new Evaluator();
        Environment env = new Environment();
        for(Node form : forms) {
            evaluator.evaluate(form, env);
        }
    }

    // Returns list of top level forms
    public List<Node> build(String program) {
        List<Node> result = new ArrayList<>();

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
                RList closedList = closeList();
                // if we're now at root level then add the list we just closed to the resulting forms
                if(currentList == null) {
                    result.add(closedList);
                }
            }
            else if(token.equals("'")){
                handleQuote();
            }
            else if(token.equals("`")) {
                newList(false);
                currentList.add(new Atom("quasiquote", null));
            }
            else if(token.startsWith("#'")){
                handleFunctionQuote();
            }
            else if(token.startsWith(",")) {
                if(token.equals(",")) {
                    newList(false);
                    currentList.add(new Atom("unquote", null));
                }
                else if(token.equals(",@")) {
                    newList(false);
                    currentList.add(new Atom("unquote-splicing", null));
                }
            }
            else {
                if(currentList != null) {
                    RList possibleTopLevelForm = addAtom(token);
                    if(possibleTopLevelForm != null) {
                        result.add(possibleTopLevelForm);
                    }
                }
                else {
                    // bare atom only
                    Atom bareAtom = new Atom(token, null);
                    result.add(bareAtom);
                }
            }
        }
        return result;
    }

    private void handleFunctionQuote() {
        newList(false);
        currentList.add(new Atom("function", null));
    }

    private void handleQuote() {
        newList(false);
        currentList.add(new Atom("quote", null));
    }

    // returns list if we're closing the top level form
    private RList addAtom(String token) {
        // if we're just about to add the third element of the list and it's dot and it's an improper list then
        // no-op
        if(currentList.improperList() && token.equals(".")) {
            if(currentList.nodes().size() == 1) {
                return null;
            }
        }
        currentList.add(new Atom(token, null));

        if(currentList.get(0) instanceof Atom possibleQuoteAtom) {
            if(TOKENS_WHICH_INSERT_AS_LIST.contains(possibleQuoteAtom.value())) {
                return closeList();
            }
        }
        return null;
    }

    // Returns list being closed
    private RList closeList() {
        RList result = currentList;
        RList parent = currentList.getParent();
        // Don't come up out of the top-level list, otherwise we have nothing left!
        currentList = parent;
        if(parent != null) {
            if(currentList.get(0) instanceof Atom possibleQuoteAtom) {
                if(TOKENS_WHICH_INSERT_AS_LIST.contains(possibleQuoteAtom.value())) {
                    RList quoteCloseResult = closeList();
                    return quoteCloseResult != null && quoteCloseResult.getParent() == null ? quoteCloseResult : null;
                }
            }
        }
        return result.getParent() == null ? result : null;
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
    }
}
