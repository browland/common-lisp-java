package compiler;

public enum Namespace {
    VARIABLE(0), FUNCTION(1);

    // Used for interface with runtime.c
    private int identifier;

    Namespace(int identifier) {
        this.identifier = identifier;
    }

    int getIdentifier() {
        return identifier;
    }
}
