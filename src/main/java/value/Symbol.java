package value;

public record Symbol(String name) {

    public boolean isKeyword() {
        return name.startsWith(":");
    }

    public boolean isConstant() {
        return isKeyword() || name.equals("t") || name.equals("nil");
    }
}
