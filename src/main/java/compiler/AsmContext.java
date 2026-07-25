package compiler;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class AsmContext {
    private List<StringLiteral> stringLiterals = new ArrayList<>();
    private List<String> taggedSymbolNames = new ArrayList<>();
    private Deque<Form> formStack = new LinkedList<>();
    private List<Form> forms = new ArrayList<>();

    void addStringLiteral(StringLiteral stringLiteral) {
        stringLiterals.add(stringLiteral);
    }

    void addTaggedSymbolName(String taggedSymbolName) {
        taggedSymbolNames.add(taggedSymbolName);
    }

    void startForm(Form form) {
        if (!formStack.isEmpty()) {
            formStack.peek().pushForm(form);
        }
        formStack.push(form);
        forms.add(form);
    }

    void endForm() {
        formStack.pop();
    }

    List<StringLiteral> getStringLiterals() {
        return stringLiterals;
    }

    public Form getCurrentFunction() {
        return formStack.peek();
    }

    public void pushInt(int i) {
        getCurrentFunction().pushInt(i);
    }

    public void withOperator(String operatorSymbol) {
        getCurrentFunction().pushOperator(operatorSymbol);
    }

    public void pushReturnValue() {
        getCurrentFunction().pushReturnValue();
    }

    public List<Form> getFunctions() {
        return forms;
    }

    public List<String> getTaggedSymbolNames() {
        return new ArrayList(taggedSymbolNames);
    }
}
