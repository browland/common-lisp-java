package repl;

import evaluator.Evaluator;
import evaluator.Value;
import reader.CharacterReader;
import syntaxtree.RList;
import syntaxtree.SyntaxTreeBuilder;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Repl {

    public static void main(String[] args) {
        // parsing and syntax tree build
        SyntaxTreeBuilder syntaxTreeBuilder = new SyntaxTreeBuilder();
        CharacterReader characterReader = new CharacterReader(syntaxTreeBuilder);

        // runtime evaluation
        Map<String,Value<?>> environment = new HashMap<>();
        Evaluator evaluator = new Evaluator();

        try {
            System.out.print("> ");
            InputStreamReader isr = new InputStreamReader(System.in);
            boolean finishedForm = false;

            // for each character in the form
            while (true) {
                char c = (char) isr.read();
                characterReader.consume(c);

                if (syntaxTreeBuilder.isFinished()) {
                    finishedForm = true;
                    endExpression(syntaxTreeBuilder, environment, evaluator);
                    System.out.print("> ");
                } else {
                    // we're in the middle of a form
                    if(c != '\n') {
                        finishedForm = false;  // reset flag; only needs doing once per form really
                    }
                    if (!finishedForm && c == '\n') {
                        System.out.print("... ");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void endExpression(SyntaxTreeBuilder syntaxTreeBuilder,
                                      Map<String, Value<?>> environment,
                                      Evaluator evaluator) {
        RList topLevelList = syntaxTreeBuilder.getResult();
        syntaxTreeBuilder.reset();
        Value<?> value = evaluator.evaluate(topLevelList, environment);
        System.out.println(value);
    }
}
