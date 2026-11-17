package evaluator;

import exception.EvaluationException;
import value.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ScriptLoader {
    private static final String LISP_SOURCES_DIR = "LISP_SOURCES_DIR";
    private static final String LISP_LIB_DIR = "LISP_LIB_DIR";

    private final Interpreter interpreter;

    public ScriptLoader(Interpreter interpreter) {
        this.interpreter = interpreter;
    }

    public void loadLibrary() throws IOException {
        String lispLibDir = getLispLibDir();
        Path lispLibPath = Path.of(lispLibDir);
        if(!Files.exists(lispLibPath)) {
            throw new RuntimeException("LISP_LIB_PATH does not exist (set to %s)".formatted(lispLibDir));
        }

        try(Stream<Path> initialFormsStream = Files.list(lispLibPath)
                .filter(path -> path.toString().endsWith("lisp"))
                .sorted()) {
            initialFormsStream.forEach(this::loadFile);
        }
    }

    private void loadFile(Path initialFormsFile) {
        try {
            List<String> lines = Files.readAllLines(initialFormsFile);
            for(String line : lines) {
                System.out.println(line);
                run(line);
            }
        }
        catch(IOException e) {
            throw new RuntimeException("While reading initial forms", e);
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

    // Can handle multiple forms
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
}
