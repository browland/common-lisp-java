package parser;

import java.util.ArrayList;
import java.util.List;

public class ParserNew {

    public static ParseResult parse(String program) {
        // the function is the first atom in the list, or the first form in the list
        Collected collected = parseFunction(program);
        Node functionNode = new Node(NodeType.FUNCTION, collected.text(), List.of());

        // throw away any spaces between function and any atoms which come next
        collected = consumeSpaces(program, collected.pos());

        List<Node> atomNodes = new ArrayList<>();
        int pos = collected.pos();
        while(pos != -1) {
            collected = parseNextAtom(program, pos);
            atomNodes.add(new Node(NodeType.ATOM, collected.text(), List.of()));
            pos = collected.pos();
            if(pos == -1) {
                break;
            }
            collected = consumeSpaces(program, pos);
            pos = collected.pos();
        }

        List<Node> nodes = new ArrayList<>();
        nodes.add(functionNode);
        nodes.addAll(atomNodes);


        return new ParseResult(new Form(nodes));
    }

    static Collected parseFunction(String form) {
        // first detect whether the function is a straightforward named function (add, let, if, etc) or is itself a form
        if(functionIsEmbedded(form)) {
            // collect chars up to the closing parenthesis
            StringBuilder sb = new StringBuilder();
            int depth = 0;
            for(int i=1; i<form.length(); i++) {  // ignore first char (outer paren for this form)
                char c = form.charAt(i);
                sb.append(c);
                if(c == '(') {
                    depth++;
                }
                else if(c == ')') {
                    depth--;
                    if(depth == 0) {
                        return new Collected(sb.toString(), i+1);  // we add 1 to pos so the next step doesn't have to deal with it
                    }
                }
            }

        }
        else {
            return parseNextAtom(form, 1);  // ignore first char (outer paren for this form)
        }
        throw new IllegalStateException("unhandled function");
    }

    public static boolean functionIsEmbedded(String form) {
        // detect whether the function is a straightforward named function (add, let, if, etc) or is itself a form
        boolean embeddedFunction = false;
        for(int i=1; i<form.length(); i++) {  // ignore first char (outer paren for this form)
            char c = form.charAt(i);
            if(c == '(') {
                embeddedFunction = true;
                break;
            }
            else if(c != ' ') {
                break;
            }
        }

        return embeddedFunction;
    }

    static Collected parseNextAtom(String form, int pos) {
        // collect chars up to the first space character
        StringBuilder sb = new StringBuilder();
        for(int i=pos; i<form.length(); i++) {  // ignore first char (outer paren for this form)
            char c = form.charAt(i);
            if(c == ' ') {  // space indicates end of atom, and don't include last bracket at end of form
                return new Collected(sb.toString(), i);
            }
            else if(c == ')') {
                return new Collected(sb.toString(), -1);
            }
            else {
                sb.append(c);
            }
        }

        // reached end of form
        return new Collected(sb.toString(), -1);
//        throw new IllegalStateException("Couldn't find end of function definition in form " + form);
    }

    static Collected consumeSpaces(String form, int pos) {
        for(int i=pos; i<form.length(); i++) {  // ignore first char (outer paren for this form)
            char c = form.charAt(i);
            if(! (c == ' ')) {
                return new Collected("", i);
            }
        }
        throw new IllegalStateException("no spaces seen and we expect one or more!");
    }

    record Collected(String text, int pos) {

    }
}
