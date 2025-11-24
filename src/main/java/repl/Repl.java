package repl;

import evaluator.Evaluator;
import evaluator.Value;
import reader.CharacterReader;
import syntaxtree.SyntaxTreeBuilder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Repl implements ReplOutput {
    private final BatchEvaluator batchEvaluator;

    public static void main(String[] args) {
        Repl repl = new Repl();
        repl.run();
    }

    public Repl() {
        SyntaxTreeBuilder syntaxTreeBuilder = new SyntaxTreeBuilder();
        CharacterReader characterReader = new CharacterReader(syntaxTreeBuilder);

        Map<String,Value<?>> environment = new HashMap<>();
        Evaluator evaluator = new Evaluator();

        batchEvaluator = new BatchEvaluator(syntaxTreeBuilder, characterReader, evaluator, environment, this);
    }

    public void run() {
        promptForNewForm();
        InputStreamReader isr = new InputStreamReader(System.in);

        try {
            while (true) {
                char c = (char) isr.read();
                batchEvaluator.consume(c);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void promptForNewForm() {
        System.out.print("> ");

    }

    @Override
    public void promptForMidForm() {
        System.out.print("... ");
    }

    @Override
    public void emitOutput(String value) {
        System.out.println(value);
    }
}
