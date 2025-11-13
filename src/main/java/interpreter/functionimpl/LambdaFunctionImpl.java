package interpreter.functionimpl;

import parser.Form;
import parser.FormBuilder;
import parser.FormTreeBuilder;
import parser.Node;

import java.util.Arrays;
import java.util.List;

public class LambdaFunctionImpl implements FunctionImpl {
    private List<String> bindingNames;
    private Form body;

    /**
     * We don't have the bound values yet; just the binding names and the lambda body
     *
     * @param bindingNames
     * @param body
     */
    public LambdaFunctionImpl(List<String> bindingNames, Form body) {
        this.bindingNames = bindingNames;
        this.body = body;
    }

    @Override
    public String apply(List<Form> arguments) {
        // At this point we can always successfully apply the lambda.  Any reduction has already been done.
        // At my current understanding, for now we generally just substitute the named argument with the incoming value
        // for that argument.
        // Step 1: extract the name for the argument from the function form
        // Step 2: extract the body of the lambda
        // Step 3: substitute the supplied argument value, for the name being substituted
        // Step 4: return the result

        // functionForm.nodes[0] is the only node.  It's of type FUNCTION, with rawText (lambda (x) (lambda (y) (+ x y)))
        // parentFormRawText == rawText because it's the top-level form being parsed from the test
        // children is empty
        // arguments is list of one Node(NodeType.ATOM, "10", "...", [])
        // we need to return: "(lambda (y) (+ 10 y))"
        return null;
    }

    public static class Builder {
        private Node operatorNode;

        public Builder(Node operatorNode) {
            this.operatorNode = operatorNode;
        }

        public LambdaFunctionImpl build() {
            // todo buggy no validation
            // todo we need to parse something like:
            //      (lambda (x) (+ x 1))
            //      Parse it manually, there shouldn't be variation; the lambda form is the lambda form
            // consume characters 'lambda' followed by a space
            String rawText = operatorNode.rawText();
            StringBuilder sb = new StringBuilder();
            int pos = 0;
            for(; ;) {
                char c = rawText.charAt(pos);
                sb.append(c);
                pos++;
                if("lambda".equals(sb.toString())) {
                    break;
                }
                else if(sb.length() > 6) {
                    throw new IllegalArgumentException("lambda syntax issue");
                }
            }

            pos = consumeSpaces(rawText, pos).pos();

            // next we have the binding names list
            // todo the problem we have here is that this is a list, data.  Do we want a special type for this (LispList)?
            //      Bear in mind a list could have elements which are forms and I suspect a form could contain a list as
            //      one operand.  So we're getting a bit complex with how things can be associated.
            //      We don't really want to smash the list concept into Form though, as those can be evaluated.
            //      It's almost as if there are fairly arbitrary nestings which can happen (A type system!) and we deal
            //      with whatever we need to at eval time.  But before then we need to be able to parse whatever is there
            //      - doing reasonable validation as we go but also loose enough to cater for the flexibility which can
            //      legally occur.
            //      A 'datum' is the right term for an atom or list, e.g. an operand or a lambda binding list.
            //      Ideally I'd just parse the List<Node> into an intermediate form which is more generic, e.g. operator
            //      and datums for example.

            List<Node> childNodes = operatorNode.children();
            Node operatorNameNode = childNodes.get(0);  // just 'lambda'; can be ignored?
            Node bindingsNode = childNodes.get(1);
            Node bodyNode = childNodes.get(2);

            // todo buggy - we make huge assumptions about the operatorNode layout - it's a top-level lambda with no child nodes
            // todo buggy - whitespace etc; fair though to assume we've fully unwrapped to single-level
            String bindingsText = bindingsNode.rawText();
            String bindingsTextNoParens = bindingsText.replace("(", "")
                    .replace(")", "");
            // bindings are comma-separated
            List<String> bindingNames = Arrays.asList(bindingsTextNoParens.split(","));

            // body - e.g. it could be (+ x 1)
            Form body = FormBuilder.build(List.of(bodyNode));

            return new LambdaFunctionImpl(bindingNames, body);
        }
    }

    record Collected(String text, int pos) {
    }

    static Collected consumeSpaces(String text, int pos) {
        for(int i=pos; i<text.length(); i++) {  // ignore first char (outer paren for this form)
            char c = text.charAt(i);
            if(! (c == ' ')) {
                return new Collected("", i);
            }
        }
        throw new IllegalStateException("no spaces seen and we expect one or more!");
    }
}
