package compiler;

public class AddAsm implements FormAsm {
    @Override
    public String generate(Form form) {
        return "add x0, x0, x1\n";
    }
}
