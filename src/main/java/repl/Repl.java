package repl;

import evaluator.Evaluator;
import evaluator.env.Environment;
import exception.EvaluationException;
import reader.CharacterReader;
import reader.NewListBuilder;
import syntaxtree.Node;
import syntaxtree.ParseElementBuilder;
import syntaxtree.SyntaxTreeBuilder;
import value.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Repl implements ReplOutput {
    private static final Repl repl = new Repl();

    private final IncrementalInterpreter incrementalInterpreter;
    private final NewListBuilder newListBuilder = new NewListBuilder();
    private final Evaluator evaluator = new Evaluator();

    public static void main(String[] args) throws IOException {
        loadLibrary();

        if(args.length == 1) {
            try {
                Path initialFormsFile = Path.of("/Users/ben/git/lisp/lisp-sources", args[0]);
                loadFile(initialFormsFile);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            repl.run();
        }
    }

    private static void loadFile(Path initialFormsFile) {
        try {
            List<String> lines = Files.readAllLines(initialFormsFile);
            for(String line : lines) {
                System.out.println(line);
                repl.run(line);
            }
        }
        catch(IOException e) {
            throw new RuntimeException("While reading initial forms", e);
        }
    }

    private static void loadLibrary() throws IOException {
        Path initialFormsDir = Path.of("/Users/ben/git/lisp/lisp-sources/lib");
        if(!Files.exists(initialFormsDir)) {
            throw new RuntimeException("does not exist");
        }

        try(Stream<Path> initialFormsStream = Files.list(initialFormsDir)) {
            initialFormsStream.forEach(Repl::loadFile);
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
                    try {
                        Value<?> result = evaluator.evaluate(form, env);
                        System.out.println(result);
                    }
                    catch(EvaluationException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            promptForNewForm();
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
