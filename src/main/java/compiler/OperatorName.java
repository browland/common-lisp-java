package compiler;

public enum OperatorName {
    ADD("_add"),
    IF("TODO"),
    DEFVAR("TODO");

    private final String asmName;

    OperatorName(String asmName) {
        this.asmName = asmName;
    }

    public String getAsmName() {
        return asmName;
    }
}
