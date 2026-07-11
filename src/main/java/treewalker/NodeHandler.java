package treewalker;

import java.util.List;

// we consider each node and dispatch to appropriate handler for that thing
public class NodeHandler implements NodeListener {

    public void handleAtom(TypedAtom<?> typedAtom) {
        switch(typedAtom) {
            case StringAtom stringAtom -> handleStringAtom(stringAtom);
            case IntAtom intAtom -> handleIntAtom(intAtom);
            case FloatAtom floatAtom -> handleFloatAtom(floatAtom);
            case CharAtom charAtom -> handleCharAtom(charAtom);
            case SymbolAtom symbolAtom -> handleSymbolAtom(symbolAtom);
            default -> throw new UnsupportedOperationException("not supported: " + typedAtom);
        }
    }

    @Override
    public void startForm() {
        // TODO should we actually emit instructions from here to begin the form we're in the process of applying?
        //      E.g. can then also handle each argument as we encounter them in e.g. handleStringAtom() etc by allocating
        //      space on stack and doing malloc and storing address on stack etc.  That would be inefficient but perhaps
        //      easier for now as we create the shape of things.
        System.out.println("start form");
    }

    @Override
    public ProcessedForm processForm(List<ProcessedNode> processedNodes) {
        String generatedFunctionName = ApplyFormFunctionGenerator.generateApplyForm(processedNodes);
        ProcessedForm processedForm = new ProcessedForm(generatedFunctionName);
        System.out.println("process form with " + processedNodes + " resulted in generated function " + processedForm);
        return processedForm;
    }

    private static void handleStringAtom(StringAtom stringAtom) {
        // TODO call generator to generate asm for string in .ro data section, returning its name, and then move it onto
        //      stack
        // TODO TypedAtom or whatever we're going to call it) could store its (arg) position so it knows its address on
        //      stack.  E.g. handleIntAtom() will receive an IntAtom, the IntAtom has its arg position so it can
        //      generate the instruction to write the appropriate literal to the right offset on the stack.  Not sure
        //      whether it's worth storing this on the TypedAtom as we'll need it again later for apply form (move
        //      stuff from stack to registers before calling the function).
        System.out.printf("encountered string atom %s with value %s%n", stringAtom, stringAtom.getValue());
    }

    private static void handleIntAtom(IntAtom intAtom) {
        System.out.printf("encountered int atom %s with value %d%n", intAtom, intAtom.getValue());
    }

    private static void handleFloatAtom(FloatAtom floatAtom) {
        System.out.printf("encountered float atom: %s with value %f%n", floatAtom, floatAtom.getValue());
    }

    private static void handleCharAtom(CharAtom charAtom) {
        System.out.printf("encountered char atom: %s with value %s%n", charAtom, charAtom.getValue());
    }

    private static void handleSymbolAtom(SymbolAtom symbolAtom) {
        System.out.printf("encountered symbol atom: %s with value %s%n", symbolAtom, symbolAtom.getValue());
    }
}
