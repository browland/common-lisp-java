import java.util.ArrayList;
import java.util.List;

public class Parser {

    static String[] splitExpressionsAtThisLevel(String program) {
        int depth = 0;
        List<String> expressions = new ArrayList<>();
        StringBuilder expressionBuilder = new StringBuilder();

        for(int i=0; i<program.length(); i++) {
            char c = program.charAt(i);

            if(c == '\n') {
                continue;
            }

            if(c == '(') {
                depth++;
            }
            else if (c == ')') {
                depth--;
            }

            if(depth == 1) {
                if ('(' == c) {
                    // no need to collect the very first opening paren
                    continue;
                }

                // we should continue collecting characters for the current expression
                if (c == ' ' || c == ')') {
                    expressionBuilder.append(c);
                    if (!expressionBuilder.toString().trim().isEmpty()) {
                        expressions.add(expressionBuilder.toString().trim());
                    }
                    expressionBuilder.delete(0, expressionBuilder.length());
                } else {
                    // we're in the middle of a nested expression
                    expressionBuilder.append(c);
                }
            }
            else if (depth == 0) {
                // we've got to the last paren, just add it to the current expression
                if(!expressionBuilder.isEmpty()) {
                    expressions.add(expressionBuilder.toString());
                    expressionBuilder.delete(0, expressionBuilder.length());
                }
            }
            else {
                // we're at a deeper level; we just always append characters to the current expression
                expressionBuilder.append(c);
            }
        }
        return expressions.toArray(new String[0]);
    }

    public static String[] extractTopLevelForms(String s) {
        // we need to split around whitespace except expressions enclosed in brackets
        StringBuilder sb = new StringBuilder();
        List<String> forms = new ArrayList<>();

        int depth = 0;  // depth within parentheses
        State state = State.START;
        for(int i=0; i<s.length();i++) {
            char c = s.charAt(i);

            // first manage depth of parentheses
            if(c == '(') {
                depth++;
            }
            else if(c == ')') {
                depth--;
            }

            if((state == State.AFTER_ATOM || state == State.AFTER_EXPRESSION) && c == ' ') {
                continue;
            }

            // first determine state
            if(state == State.START || state == State.AFTER_ATOM || state == State.AFTER_EXPRESSION) {
                if(c == '(') {
                    state = State.ON_EXPRESSION;
                }
                else {
                    state = State.ON_ATOM;
                }
            }
            else if(state == State.ON_ATOM) {
                if(c == ' ') {
                    state = State.AFTER_ATOM;
                }
            }
            else if (state == State.ON_EXPRESSION) {
                if(c == ')' && depth == 0) {
                    state = State.AFTER_EXPRESSION;
                }
            }

            if(state == State.ON_ATOM || state == State.ON_EXPRESSION) {
                sb.append(c);
            }
            else if(state == State.AFTER_ATOM) {
                forms.add(sb.toString());
                sb.delete(0, sb.length());
            }
            else if(state == State.AFTER_EXPRESSION) {
                sb.append(c);
                forms.add(sb.toString());
                sb.delete(0, sb.length());
            }
        }

        if(state == State.ON_ATOM) {
            forms.add(sb.toString());
            sb.delete(0, sb.length());
        }
        else if(state == State.ON_EXPRESSION) {
            forms.add(sb.toString());
            sb.delete(0, sb.length());
        }

        return forms.toArray(new String[] {});
    }

    static boolean isLambda(String expression) {
        if(!expression.contains("lambda")) {
            return false;
        }

        // we iteratively split expressions at top level for the first expression, until there are no more parens at the start
        // if the resulting string starts with 'lambda' then this is a lambda expression
        String resultingFirstExpression = expression;
        while(resultingFirstExpression.startsWith("(")) {
            resultingFirstExpression = Parser.splitExpressionsAtThisLevel(resultingFirstExpression)[0];
        }

        return resultingFirstExpression.startsWith("lambda");
    }

    enum State {
        START, ON_ATOM, ON_EXPRESSION, AFTER_ATOM, AFTER_EXPRESSION
    }
}
