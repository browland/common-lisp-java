package repl;

public interface ReplOutput {
    void promptForNewForm();
    void promptForMidForm();
    void emitOutput(String value);
}
