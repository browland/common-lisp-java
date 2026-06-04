package repl;

import evaluator.Evaluator;
import evaluator.env.Environment;
import exception.EvaluationException;
import reader.CharacterReader;
import reader.NewListBuilder;
import reader.SimplerTokeniser;
import syntaxtree.Node;
import syntaxtree.ParseElementBuilder;
import syntaxtree.SyntaxTreeBuilder;
import value.Value;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Repl implements ReplOutput {
    private final IncrementalInterpreter incrementalInterpreter;
    private final NewListBuilder newListBuilder = new NewListBuilder();
    private final Evaluator evaluator = new Evaluator();

    public static void main(String[] args) throws IOException {
        Repl repl = new Repl();

        if(args.length == 1) {
            try {
                Path initialFormsFile = Path.of("/Users/ben/git/lisp/lisp-sources", args[0]);
                List<String> lines = Files.readAllLines(initialFormsFile);
                for(String line : lines) {
                    System.out.println(line);
                    repl.run(line);
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            repl.run();
        }
    }

    public Repl() {
        SyntaxTreeBuilder syntaxTreeBuilder = new SyntaxTreeBuilder();
        ParseElementBuilder parseElementBuilder = new ParseElementBuilder(syntaxTreeBuilder);
        CharacterReader characterReader = new CharacterReader(parseElementBuilder);

        Environment environment = new Environment();
        Evaluator evaluator = new Evaluator();

        incrementalInterpreter = new IncrementalInterpreter(syntaxTreeBuilder, parseElementBuilder, characterReader, evaluator, environment, this);
    }

    public void run(String initialForms) {
        for(char c : initialForms.toCharArray()) {
            try {
                incrementalInterpreter.consume(c);
            }
            catch(EvaluationException e) {
                System.err.println(e.getMessage() + "\n");
            }
        }
        incrementalInterpreter.consume('\n');  // signal end of line; required to know when each line is done
    }

    public void run() throws IOException{
        promptForNewForm();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        Environment env = new Environment();
        while(true) {
            String line = br.readLine();
            List<Node> forms = newListBuilder.build(line);
            if(forms != null) {
                for(Node form : forms) {
                    Value<?> result = evaluator.evaluate(form, env);
                    System.out.println(result);
                }
            }
            promptForNewForm();
        }
//        InputStreamReader isr = new InputStreamReader(System.in);
//
//        try {
//            while (true) {
//                char c = (char) isr.read();
//                try {
//                    incrementalInterpreter.consume(c);
//                }
//                catch(EvaluationException e) {
//                    System.err.println(e.getMessage() + "\n");
//                }
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
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
