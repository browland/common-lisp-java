package compiler.treewalker;

import compiler.*;
import compiler.specialform.*;
import reader.NodeBuilder;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Beginnings of compiler and might end up being retro-fitted to the existing interpreter as a general case of tree-
// walking.
public class TreeWalker {
    private final AsmGenerator asmGenerator = new AsmGenerator();
    private final Map<String, SpecialForm> specialForms = new HashMap<>();
    private final Map<String, Function> functions = new HashMap<>();

    public TreeWalker() {
        specialForms.put("if", new IfSpecialForm());
        specialForms.put("defun", new DefunSpecialForm());
        specialForms.put("defvar", new DefvarSpecialForm());
        specialForms.put("lambda", new LambdaSpecialForm());

        functions.put("add", new Function("add", Map.of()));
        functions.put("+", new Function("add", Map.of()));
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NodeBuilder nodeBuilder = new NodeBuilder();

        TreeWalker walker = new TreeWalker();

        // String atom
//        String program = "1";
//        String program = "(if t 1 2)";
//        String program = "(add 1 2)";
//        String program = "(+ 1 2)";
//        String program = "(+ 1 (+ 1 2))";
//        String program = "(defun foo () 2) (foo)";
//        String program = "(defun foo () (add 1 1)) (if t (foo) (add 1 2))";
//        String program = "(defun foo (x y) x) (foo 1 2)";
//        String program = "(defvar x (+ 1 1)) (if t x (+ 1 2))";
//        String program = "(defvar x 2) (if nil nil x)";
//        String program = "(defun foo (x y) (+ x y)) (foo 1 2)";
        String program = "((lambda (x) (+ x 1)) 1)";

        List<Node> nodes = nodeBuilder.build(program);
        walker.walkTopLevelNodes(nodes);

        // Assemble
        // We use -falign-functions=8 to ensure we can use pointer tagging for our built-in functions.  We could mark
        // each function with __attribute__((aligned(8))) but this seems a cleaner option and less likely to forget
        // a case.  At the cost of a larger executable due to the extra padding needed.
        Process clangProcess = Runtime.getRuntime().exec(new String[] {"clang", "-falign-functions=8", "./src/main/asm/my-asm.s", "./src/main/c/runtime.c"});
        InputStream clangStandardError = clangProcess.getErrorStream();
        InputStreamReader clangStandardErrorReader = new InputStreamReader(clangStandardError);
        BufferedReader clangStandardErrorBufferedReader = new BufferedReader(clangStandardErrorReader);
        String line;
        while ((line = clangStandardErrorBufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        int clangExitCode = clangProcess.waitFor();
        if (clangExitCode != 0) {
            System.out.println("assemble step failed, exit code: " + clangExitCode);
            System.exit(0);
        }
        else {
            System.out.println("assemble step successful");
        }

    }

    // We walk through each node in turn at this level, recursing for any list encountered in any of the Node positions.
    // Once we end up with the evaluated list of nodes, then we evaluate the "flattened" list at this level as a form.
    // We call into our NodeListener for individual atoms as well as the overall form at each level while we still
    // evolve the design.
    void walkTopLevelNodes(List<Node> nodes) throws IOException {
        asmGenerator.initMainFunction();

        for (Node node : nodes) {
            walkTree(node, null);
        }

        asmGenerator.printResultAndCleanUpMainFunction();

        BufferedWriter bw = new BufferedWriter(new FileWriter("./src/main/asm/my-asm.s"));

        asmGenerator.dumpAsm(bw);
    }

    public void walkTree(Node node, Function currentFunctionScope) {
        if (node instanceof Atom atom) {
            handleAtom(atom, currentFunctionScope);
        }
        else if (node instanceof RList rlist) {
            walkTree(rlist, currentFunctionScope);
        }
    }

    private TypedAtom<?> handleAtom(Atom atom, Function currentFunctionScope) {
        TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
        if (typedAtom instanceof SymbolAtom symbolAtom) {
            String symbol = symbolAtom.getValue();
            if (currentFunctionScope.containsBinding(symbol)) {
                int stackOffset = currentFunctionScope.getStackOffsets().get(symbol);
                asmGenerator.loadOperandFromStackOffsetIntoRegister(stackOffset, 0);
            }
            else {
                // check our mappings to stack offsets first (bindings for the current function)
                asmGenerator.generateSymbolLookup(symbolAtom.getValue(), Namespace.VARIABLE);
            }
        }
        else if (typedAtom instanceof IntAtom intAtom) {
            asmGenerator.writeFixNumToRegister(0, intAtom.getFixNum());
        }
        else {
            throw new UnsupportedOperationException("unsupported to eval other types of atoms");

        }
        return typedAtom;
    }

    private void walkTree(RList rlist, Function currentFunctionScope) {
        if (rlist.nodes().getFirst() instanceof Atom operatorAtom) {
            TypedAtom<?> ta = TypedAtom.fromAtom(operatorAtom);
            if (ta instanceof SymbolAtom sa) {
                // First check operator symbol for match on special forms which are statically defined.
                SpecialForm specialForm = specialForms.get(sa.getValue());
                if (specialForm != null) {
                    specialForm.walkTree(rlist, this, asmGenerator, currentFunctionScope);
                }
                else {
                    Function function = functions.get(sa.getValue());
                    if (function != null) {
                        walkTreeForFunctionCall(rlist, function, currentFunctionScope);
                    }
                }
            }
            else {
                throw new UnsupportedOperationException("can't coerce type of operator atom");
            }

        }
        else {
            // TODO inline lambda application - the only valid case where we can have a list in the first position in a form
            //      Handle the list recursively - the result of evaluation will be a tagged ptr for the closure value
            //      and we'll continue evaluating the current form once we unwind back to this level and apply it.
//            walkTree(rlist.nodes().getFirst(), currentFunctionScope);
            // TODO how to continue processing the form?  Get Function from functions by name; but we don't know its name ...
            //      Then walkTreeForFunctionCall.
            //      For now we know it's called closure_0
            Function closure = functions.get("closure_0");
            walkTreeForFunctionCall(rlist, closure, currentFunctionScope);
        }
    }

    private void walkTreeForFunctionCall(RList rlist, Function functionToCall, Function currentFunctionScope) {
        // We're evaluating a form.
        // Depending on the operand count, we know how much stack to reserve to hold them.
        int numOperands = rlist.size()-1;

        // We need 16 bytes for each operand plus one for the function pointer; but ensure we always reserve a multiple
        // of 16 bytes
        final int stackBytes = (int)(16 * Math.ceil(numOperands+1/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes);

        // This doubles as the stack slot index, and also the node index of the list we're walking
        int slot = 0;

        for (Node childNode : rlist.nodes()) {
            if (childNode instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
                if (typedAtom instanceof  IntAtom intAtom) {
                    long fixNum = intAtom.getFixNum();
                    asmGenerator.pushFixNumToStack(slot++, fixNum);
                }
                else if (typedAtom instanceof SymbolAtom symbolAtom) {
                    if (slot == 0) {
                        // operator position; look up symbol from function namespace.  This will return a function ptr.
                        // we'll then write the function pointer after the operands (stackSlot was already post-incremented on last operand)
                        asmGenerator.generateSymbolLookup(functionToCall.getSymbolStringName(), Namespace.FUNCTION);
                        asmGenerator.untagFunctionPtr();
                        asmGenerator.storeResultToStack(slot++);
                    }
                    else {
                        String symbol = symbolAtom.getValue();
                        if (currentFunctionScope.containsBinding(symbol)) {
                            int stackOffset = currentFunctionScope.getStackOffsets().get(symbol);
                            asmGenerator.loadOperandFromStackOffsetIntoRegister(stackOffset, 0);
                        }
                        else {
                            // Generate the global variable for this symbol
                            asmGenerator.generateDataSectionQuadWordForSymbolPtr(symbol);
                            // TODO don't add same symbol to asm .cstring segment more than once; sort and uniq them while flushing to asm output for e.g.

                            asmGenerator.generateSymbolLookup(symbol, Namespace.VARIABLE);
                        }
                        asmGenerator.storeResultToStack(slot++);  // TODO code smell
                    }
                }
            }
            else if (childNode instanceof RList innerRList) {
                // We have a form to evaluate.
                // Processing of the inner form will recursively write assembly like we are here; the result will be in
                // x0 so we write it to the next pos on our stack of evaluated operands for this form.
                walkTree(innerRList, currentFunctionScope);
                if (slot == 0) {
                    functionToCall = functions.get("closure_0");  // TODO hacking this in for now
                    asmGenerator.generateSymbolLookup(functionToCall.getSymbolStringName(), Namespace.FUNCTION);
                    asmGenerator.untagFunctionPtr();
                }
                asmGenerator.storeResultToStack(slot++);
            }
        }

        // Now the evaluated operands are on the stack, load them into registers ready for our operator call
        // Operands will be stored in registers in incrementing order as per usual calling convention
        for (int operandNum = 0; operandNum<numOperands; operandNum++) {
            // Zero-indexed operands are aligned with registers, but stack pos is one pos higher (above the function ptr slot)
            asmGenerator.loadOperandFromStackIntoRegister(operandNum+1, operandNum);
        }

        // Load function ptr into next available register
        // We use operandNum as it's already incremented to next register num (it's not an operand but we need it in
        // some register for the jump).
        int functionPtrRegister = numOperands; // Function ptr will be stored in the next register after those used by the operands
        asmGenerator.loadOperandFromStackIntoRegister(0, functionPtrRegister);

        asmGenerator.callFunction(functionPtrRegister);
        asmGenerator.freeSpaceOnStack(stackBytes);
    }

    public Map<String,Function> getFunctions() {
        return functions;
    }
}
