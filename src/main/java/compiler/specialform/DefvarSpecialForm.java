package compiler.specialform;

import compiler.AsmGenerator;
import syntaxtree.Node;
import syntaxtree.Atom;
import syntaxtree.RList;
import treewalker.*;

public class DefvarSpecialForm implements SpecialForm {
    private static long LABEL_COUNTER = 0L;

    @Override
    public void walkTree(RList rlist, TreeWalker treeWalker, AsmGenerator asmGenerator) {
        // generate asm to determine tagged symbol ptr for symbol in node 1.  We know there'll be a runtime symbol for it as we'll 
        // generate one as part of defvar.  So generate the code to retrieve the tagged ptr from this variable, and pass that into 
        // _get_sym which we'll define in runtime.c
        Node symbolNode = rlist.get(1);
        if (symbolNode instanceof Atom atom) {
            TypedAtom typedAtom = TypedAtom.fromAtom(atom);
            if (typedAtom instanceof SymbolAtom symbolAtom) {
                String symbolValue = symbolAtom.getValue();

                // Generate the global variable for this symbol
                asmGenerator.generateTaggedSymbolName(symbolValue);

                asmGenerator.reserveSpaceOnStack(16);
                
                // Load symbol tagged ptr into x0
                asmGenerator.generateLoadSymbolTaggedPtr(symbolValue);

                // Stash the tagged symbol ptr on the stack in case generateSymbolExists clobbers x0
                asmGenerator.storeResultToStack(0);

                // Call _symbol_exists
                asmGenerator.generateSymbolExists();

                // x0 now contains either an address of the existing value from the symbol table (variable namespace) or NULL.
                // generate code for the exit label
                long thisLabelCounter = LABEL_COUNTER++;
                String exitLabel = "defvar_exit" + thisLabelCounter;
                // Generate jump instruction to jump to exit label if x0 is not zero (symbol already defined, so we no-op).
                asmGenerator.generateJumpInstructionForNonZeroReturnValue(exitLabel);

                // Evaluate value node; the result will end up in x0
                Node valueNode = rlist.nodes().get(2);
                treeWalker.walkTree(valueNode);

                // Ultimately we want to call roughly put_symbol(symbol_ptr, value_ptr) so we need to move value ptr into x1 to free x0 for symbol ptr
                asmGenerator.write("mov x1, x0  ; move result of evaluating defvar value into second arg before calling _put_symbol\n");
 
                // Load tagged symbol ptr back off stack into x0
                asmGenerator.loadOperandFromStackIntoRegister(0);

                // Call into runtime put_symbol
                asmGenerator.write("bl _put_symbol \n");

                // Generate exit label once code to write the new symbol value has been generated
                asmGenerator.generateLabel(exitLabel);
 
                // Free space on stack
                asmGenerator.freeSpaceOnStack(16);
            }
        }
    }
}
