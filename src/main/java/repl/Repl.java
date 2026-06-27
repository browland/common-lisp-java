package repl;

import evaluator.Evaluator;
import evaluator.env.Environment;
import exception.EvaluationException;
import function.FunctionDefinitions;
import reader.NodeBuilder;
import syntaxtree.Node;
import value.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class Repl {
    private static final String LISP_SOURCES_DIR = "LISP_SOURCES_DIR";
    private static final String LISP_LIB_DIR = "LISP_LIB_DIR";

    private static final Repl repl = new Repl();

    private final NodeBuilder nodeBuilder = new NodeBuilder();
    private final Evaluator evaluator = new Evaluator();
    private final Environment env = new Environment();

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

    private static void loadLibrary() throws IOException {
        String lispLibDir = getLispLibDir();
        Path lispLibPath = Path.of(lispLibDir);
        if(!Files.exists(lispLibPath)) {
            throw new RuntimeException("LISP_LIB_PATH does not exist (set to %s)".formatted(lispLibDir));
        }

        try(Stream<Path> initialFormsStream = Files.list(lispLibPath)
                .filter(path -> path.toString().endsWith("lisp"))
                .sorted()) {
            initialFormsStream.forEach(Repl::loadFile);
        }
    }

    public void run(String initialForms) throws IOException {
        BufferedReader br = new BufferedReader(new StringReader(initialForms));
        String line = br.readLine();

        while(line != null) {
            List<Node> forms = nodeBuilder.build(line);
            if (forms != null) {
                for (Node form : forms) {
                    try {
                        Value<?> result = evaluator.evaluate(form, env);
                        System.out.println(result);
                    } catch (EvaluationException e) {
                        System.out.println(e.getMessage());
                    }
                }
            }
            line = br.readLine();
        }
    }

    private void init() throws IOException {
        FunctionDefinitions.addFunctionDefinitions(env);
        loadLibrary();
    }

    public void run() throws IOException{
        init();
        promptForNewForm();
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        while(true) {
            String line = br.readLine();
            List<Node> forms = nodeBuilder.build(line);
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
                promptForNewForm();
            }
            else {
                promptForMidForm();
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

    private static String getLispLibDir() {
        String libDirEnvVar = System.getenv(LISP_LIB_DIR);
        if(libDirEnvVar == null) {
            throw new RuntimeException("Please set environment variable LISP_LIB_DIR");
        }
        return libDirEnvVar;
    }

    public void promptForNewForm() {
        System.out.print("> ");
    }

    public void promptForMidForm() {
        System.out.print("... ");
    }
}
