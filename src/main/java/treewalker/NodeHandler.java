package treewalker;

import compiler.AsmContext;
import compiler.AsmGenerator;

import java.util.List;

// we consider each node and dispatch to appropriate handler for that thing
public class NodeHandler implements NodeListener {

    private final AsmContext context = new AsmContext();
    private final AsmGenerator generator = new AsmGenerator();

    public void handleAtom(TypedAtom<?> typedAtom, int pos) {
        switch(typedAtom) {
            case StringAtom stringAtom -> handleStringAtom(stringAtom, pos);
            case IntAtom intAtom -> handleIntAtom(intAtom, pos);
            case FloatAtom floatAtom -> handleFloatAtom(floatAtom, pos);
            case CharAtom charAtom -> handleCharAtom(charAtom, pos);
            case SymbolAtom symbolAtom -> {
                if (pos == 0) {
                    handleOperatorNode(symbolAtom);
                }
                else {
                    handleSymbolAtom(symbolAtom, pos);
                }
            }
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
        generator.startForm(context);
    }

    @Override
    public ProcessedForm processForm(List<ProcessedNode> processedNodes) {
        generator.endForm(context);

        //String generatedFunctionName = ApplyFormFunctionGenerator.generateApplyForm(processedNodes);
        //ProcessedForm processedForm = new ProcessedForm(generatedFunctionName);
        //System.out.println("process form with " + processedNodes + " resulted in generated function " + processedForm);
        //return processedForm;
        return null;  // todo ProcessedForm now redundant?
    }

    // TODO should return something more general (to handle interpreter mode) OR make the context which we're passing
    //      around more general.
    public String endTree() {
        //return generator.generate(context);
        return null;  // TODO dead code
    }

    private void handleStringAtom(StringAtom stringAtom, int pos) {
        // TODO call generator to generate asm for string in .ro data section, returning its name, and then move it onto
        //      stack
        // TODO TypedAtom or whatever we're going to call it) could store its (arg) position so it knows its address on
        //      stack.  E.g. handleIntAtom() will receive an IntAtom, the IntAtom has its arg position so it can
        //      generate the instruction to write the appropriate literal to the right offset on the stack.  Not sure
        //      whether it's worth storing this on the TypedAtom as we'll need it again later for apply form (move
        //      stuff from stack to registers before calling the function).
        System.out.printf("encountered string atom %s with value %s%n", stringAtom, stringAtom.getValue());
    }

    private void handleIntAtom(IntAtom intAtom, int pos) {
        System.out.printf("encountered int atom %s with value %d%n", intAtom, intAtom.getValue());
        generator.pushInt(intAtom.getValue(), context);
    }

    private void handleFloatAtom(FloatAtom floatAtom, int pos) {
        System.out.printf("encountered float atom: %s with value %f%n", floatAtom, floatAtom.getValue());
    }

    private void handleCharAtom(CharAtom charAtom, int pos) {
        System.out.printf("encountered char atom: %s with value %s%n", charAtom, charAtom.getValue());
    }

    private void handleSymbolAtom(SymbolAtom symbolAtom, int pos) {
        System.out.printf("encountered symbol atom in argument pos: %s with value %s%n", symbolAtom, symbolAtom.getValue());
    }

    private void handleOperatorNode(SymbolAtom symbolAtom) {
        System.out.printf("encountered symbol atom in operator pos: %s with value %s%n", symbolAtom, symbolAtom.getValue());
        generator.withOperator(symbolAtom.getValue(), context);
    }
}
