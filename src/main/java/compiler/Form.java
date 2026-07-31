package compiler;

/**
 * A form is made up by an operator followed by the appropriate list of operands which can be either primitives
 * or more forms (which implies another call is being made).
 * E.g.:
 *  [+, 1, Form]
 *  could relate to the top-level form in:
 *  (+ 1 (+ 1 2))
 *
 */
public class Form {
    private final String asmFunctionName;
    // Unevaluated parts of this form, including operator - not evaluated, just straight from the tree

    public Form(String asmFunctionName) {
        this.asmFunctionName = asmFunctionName;
    }

    public String getAsmFunctionName() {
        return asmFunctionName;
    }
}
