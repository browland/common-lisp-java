package repl;

import evaluator.Evaluator;
import evaluator.env.Environment;
import reader.CharacterReader;
import syntaxtree.ParseElementBuilder;
import syntaxtree.SyntaxTreeBuilder;

import java.io.IOException;
import java.io.InputStreamReader;

public class Repl implements ReplOutput {
    private final IncrementalInterpreter incrementalInterpreter;

    static void main(String[] args) {
        Repl repl = new Repl();
        repl.run();
    }

    public Repl() {
        SyntaxTreeBuilder syntaxTreeBuilder = new SyntaxTreeBuilder();
        ParseElementBuilder parseElementBuilder = new ParseElementBuilder(syntaxTreeBuilder);
        CharacterReader characterReader = new CharacterReader(parseElementBuilder);

        Environment environment = new Environment();
        Evaluator evaluator = new Evaluator();

        incrementalInterpreter = new IncrementalInterpreter(syntaxTreeBuilder, parseElementBuilder, characterReader, evaluator, environment, this);
    }

    public void run() {
        promptForNewForm();
        InputStreamReader isr = new InputStreamReader(System.in);

        try {
            while (true) {
                char c = (char) isr.read();
                incrementalInterpreter.consume(c);
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
