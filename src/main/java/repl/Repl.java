package repl;

import evaluator.Evaluator;
import evaluator.Value;
import reader.CharacterReader;
import syntaxtree.RList;
import syntaxtree.SyntaxTreeBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Repl {

    public static void main(String[] args) {
        // parsing and syntax tree build
        SyntaxTreeBuilder syntaxTreeBuilder = new SyntaxTreeBuilder();
        CharacterReader characterReader = new CharacterReader(syntaxTreeBuilder);

        // runtime evaluation
        Map<String,String> environment = new HashMap<>();
        Evaluator evaluator = new Evaluator();

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        while(true) {
            try {
                System.out.print("> ");
                String line = br.readLine();
                if(line.isBlank()) {
                    endExpression(syntaxTreeBuilder, environment, evaluator);
                }
                else {
                    characterReader.read(line);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void endExpression(SyntaxTreeBuilder syntaxTreeBuilder,
                                      Map<String, String> environment,
                                      Evaluator evaluator) {
        RList topLevelList = syntaxTreeBuilder.getResult();
        syntaxTreeBuilder.reset();
        Value<?> value = evaluator.evaluate(topLevelList, environment);
        System.out.println(value);
    }
}
