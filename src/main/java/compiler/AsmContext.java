package compiler;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class AsmContext {
    private List<StringLiteral> stringLiterals = new ArrayList<>();
    private Deque<Function> functionStack = new LinkedList<>();
    private List<Function> functions = new ArrayList<>();

    void addStringLiteral(StringLiteral stringLiteral) {
        stringLiterals.add(stringLiteral);
    }

    void startFunction(Function function) {
        if (!functionStack.isEmpty()) {
            functionStack.peek().pushFunction(function);
        }
        functionStack.push(function);
        functions.add(function);
    }

    void endFunction() {
        functionStack.pop();
    }

    List<StringLiteral> getStringLiterals() {
        return stringLiterals;
    }

    public Function getCurrentFunction() {
        return functionStack.peek();
    }

    public void pushInt(int i) {
        getCurrentFunction().pushInt(i);

    }

    public void withOperator(String op) {
        getCurrentFunction().pushOperator(op);
    }

    public void pushReturnValue() {
        getCurrentFunction().pushReturnValue();
    }

    public List<Function> getFunctions() {
        return functions;
    }
}
