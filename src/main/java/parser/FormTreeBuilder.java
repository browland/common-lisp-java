package parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Parses a form, i.e. a list which can be evaluated.  This only goes as far as yielding a list of Node objects, which
 * indicate the type of node (operator or operand) and encapsulates the text for that individual node.  Each node also
 * has a reference to its children, for example nested lambdas.  The enclosing form is also set on each node, which is
 * needed when substituting a child form with its evaluated value, within the parent form.
 *
 * We assume there's always an operator followed by operands.
 *
 * There should be another richer parsing layer above this one which:
 * - accepts as input the List<Node> produced by this layer
 * - parses each Node, resulting in an enriched representation of the form, which knows what type of form it is (special
 *   forms etc), with meaningful state derived from the operands.  Type checking and validation would be done here.
 * - the enriched form would also have the impl for the particular operator in play
 * - it would then be easy to evaluate this enriched form without any outside co-ordination by calling apply() on it
 * I really want to stick to another separate layer rather than dumping more complexity here.  Maybe though we can share
 * some parsing code as there will be some overlap.
 * Naming of the new layer: FormBuilder
 */
public class FormTreeBuilder {

    public static List<Node> parse(String form) {
        Collected collected = parseOperator(form);
        Node operatorNode = new Node(NodeType.OPERATOR, collected.text(), form, collected.childNodes());

        // if this list contains only a function and nothing more, then exit early.  For now we treat this as OK, but
        // may end up being invalid.  I thought I needed it for a test case but may end up redundant.
        if(collected.pos() > form.length()-1) {
            return List.of(operatorNode);
        }

        // throw away any spaces between the consumed operator and any operands which come next
        collected = consumeSpaces(form, collected.pos());

        List<Node> operandNodes = new ArrayList<>();
        int pos = collected.pos();
        while(pos != -1) {
            collected = parseNextAtom(form, pos);
            operandNodes.add(new Node(NodeType.OPERAND, collected.text(), form, List.of()));
            pos = collected.pos();
            if(pos == -1) {
                break;
            }
            collected = consumeSpaces(form, pos);
            pos = collected.pos();
        }

        List<Node> nodes = new ArrayList<>();
        nodes.add(operatorNode);
        nodes.addAll(operandNodes);

        return nodes;
    }

    /**
     * We recursively parse the form, parsing any forms found within.  We do this until each collected form could be
     * evaluated.
     */
    static Collected parseOperator(String form) {
        // first detect whether the operator is:
        // * a *function form* (a form which yields a function), e.g. a lambda or something else
        // * not itself a form, e.g. add, let, if, or a symbol naming a function
        if(isAForm(form)) {
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
                        String parsedOperatorText = sb.toString();
                        List<Node> childNodes = containsEmbeddedForm(parsedOperatorText) ? parse(parsedOperatorText) : List.of();
                        return new Collected(parsedOperatorText, i+1, childNodes);  // we add 1 to pos so the next step doesn't have to deal with it
                    }
                }
            }
        }
        else {
            return parseNextAtom(form, 1);  // ignore first char (outer paren for this form)
        }
        throw new IllegalStateException("unhandled function");
    }

    public static boolean isAForm(String text) {
        // detect whether the text is a form
        boolean isForm = false;
        for(int i=1; i<text.length(); i++) {  // ignore first char (outer paren for this form)
            char c = text.charAt(i);
            if(c == '(') {
                isForm = true;
                break;
            }
            else if(c != ' ') {
                break;
            }
        }

        return isForm;
    }

    static Collected parseNextAtom(String form, int pos) {
        // collect chars up to the first space character
        StringBuilder sb = new StringBuilder();
        for(int i=pos; i<form.length(); i++) {  // ignore first char (outer paren for this form)
            char c = form.charAt(i);
            if(c == ' ') {  // space indicates end of atom, and don't include last bracket at end of form
                return new Collected(sb.toString(), i, List.of());
            }
            else if(c == ')') {
                return new Collected(sb.toString(), -1, List.of());
            }
            else {
                sb.append(c);
            }
        }

        // reached end of form
        return new Collected(sb.toString(), -1, null);
    }

    static Collected consumeSpaces(String form, int pos) {
        for(int i=pos; i<form.length(); i++) {  // ignore first char (outer paren for this form)
            char c = form.charAt(i);
            if(! (c == ' ')) {
                return new Collected("", i, null);
            }
        }
        throw new IllegalStateException("no spaces seen and we expect one or more!");
    }

    static boolean containsEmbeddedForm(String text) {
        if(!FormTreeBuilder.isAForm(text)) {
            return false;
        }

        int depth = 0;
        for(int i = 0; i< text.length(); i++) {
            char c = text.charAt(i);
            if(c == '(') {
                depth++;
                if(depth > 1) {
                    return true;
                }
            }
            else if(c == ' ') {
                continue;  // spaces are ignored for this
            }
            else {
                return false;  // only open-brackets can make a difference
            }
        }
        throw new IllegalStateException("can't get here");
    }

    record Collected(String text, int pos, List<Node> childNodes) {
    }
}
