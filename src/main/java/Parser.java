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
}
