package compiler;

import java.util.ArrayList;
import java.util.List;

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
    private final List<Object> rawParts = new ArrayList<>();
    private Operator operator;

    public Form(String asmFunctionName) {
        this.asmFunctionName = asmFunctionName;
    }

    public void pushInt(int i) {
        rawParts.add(i);
    }

    public void pushOperator(String operatorSymbol) {
        Operator operator = Operator.fromSymbol(operatorSymbol);
        rawParts.add(operator);
        this.operator = operator;
    }

    public void pushForm(Form form) {
        rawParts.add(form);
    }

    public void pushReturnValue() {
        rawParts.add(new ReturnValue());
    }

    public String getAsmFunctionName() {
        return asmFunctionName;
    }

    public List<Object> getRawParts() {
        return rawParts;
    }

    public Operator getOperator() {
        return operator;
    }
}
