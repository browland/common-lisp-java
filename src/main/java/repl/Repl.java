package repl;

import evaluator.Interpreter;
import evaluator.ScriptLoader;
import evaluator.env.Environment;
import exception.EvaluationException;
import function.FunctionDefinitions;
import syntaxtree.ReaderException;
import value.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

public class Repl {
    private static final String LISP_SOURCES_DIR = "LISP_SOURCES_DIR";

    private static final Repl repl = new Repl();

    private final Environment env = new Environment();
    private final Interpreter interpreter = new Interpreter(env);
    private final ScriptLoader scriptLoader = new ScriptLoader(interpreter);

    public static void main(String[] args) throws IOException {
        if(args.length == 1) {
            try {
                repl.init();
                String lispSourcesDir = getLispSourcesDir();
                Path initialFormsFile = Path.of(lispSourcesDir, args[0]);
                loadFile(initialFormsFile);
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        else {
            repl.init();
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

    // Takes multiple forms in one big string, evals one line at a time and prints result of each
    public void run(String initialForms) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(initialForms));
        String line = br.readLine();

        try {
            while (line != null) {
                Optional<List<Value<?>>> optionalValues = interpreter.interpretPartial(line);
                optionalValues.ifPresent(values -> System.out.println(values.getLast()));

                line = br.readLine();
            }
        }
        catch(EvaluationException e) {
            System.out.println(e.getMessage());
            throw e;
        }
    }

    private void init() throws IOException {
        FunctionDefinitions.addFunctionDefinitions(env);
        scriptLoader.loadLibrary();
    }

    public void run() throws IOException{
        init();
        promptForNewForm();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            String line = br.readLine();

            try {
                Optional<List<Value<?>>> optionalValues = interpreter.interpretPartial(line);
                if (optionalValues.isPresent()) {
                    List<Value<?>> values = optionalValues.get();
                    System.out.println(values.getLast());
                    promptForNewForm();
                }
                else {
                    promptForMidForm();
                }
            }
            catch(ReaderException e) {
                System.out.println(e.getMessage());
                promptForNewForm();
            }
            catch(EvaluationException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    private static String getLispSourcesDir() {
        String sourcesDirEnvVar = System.getenv(LISP_SOURCES_DIR);
        if(sourcesDirEnvVar == null) {
            throw new RuntimeException("Please set environment variable LISP_SOURCES_DIR");
        }
        return sourcesDirEnvVar;
    }

    public void promptForNewForm() {
        System.out.print("> ");
    }

    public void promptForMidForm() {
        System.out.print("... ");
    }
}
