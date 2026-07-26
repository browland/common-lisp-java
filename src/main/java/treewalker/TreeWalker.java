package treewalker;

import compiler.*;
import compiler.specialform.*;
import reader.NodeBuilder;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.io.*;
import java.util.List;

// Beginnings of compiler and might end up being retro-fitted to the existing interpreter as a general case of tree-
// walking.
public class TreeWalker {
    private final AsmGenerator asmGenerator = new AsmGenerator();

    public static void main(String[] args) throws IOException, InterruptedException {
        NodeBuilder nodeBuilder = new NodeBuilder();

        TreeWalker walker = new TreeWalker();

        // String atom
        String program = "(add 1 2)";
//        String program = "(+ 1 (+ 1 2))";
//        String program = "(defun foo (+ 1 1)) (defvar x (+ 1 1)) (if t x (+ 1 2))";
//        String program = "(defvar x (+ 1 1)) (if t x (+ 1 2))";
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

        // Run and print result
        //Process execProcess = Runtime.getRuntime().exec(new String[] {"./a.out"});

        //InputStream execStandardOut = execProcess.getInputStream();
        //InputStreamReader execStandardOutReader = new InputStreamReader(execStandardOut);
        //BufferedReader execStandardOutBufferedReader = new BufferedReader(execStandardOutReader);

        //InputStream execStandardError = execProcess.getErrorStream();
        //InputStreamReader execStandardErrorReader = new InputStreamReader(execStandardError);
        //BufferedReader execStandardErrorBufferedReader = new BufferedReader(execStandardErrorReader);

        //int execExitCode = execProcess.waitFor();
        //if (execExitCode != 0) {
        //    System.out.printf("executable failed to run, exit code: %d, run directly to get output%n", execExitCode);
            // It's actually some kind of signal(?) and the shell is what's printing something like zsh: bus error ./a.out
//            while ((line = execStandardOutBufferedReader.readLine()) != null) {
//                System.out.println(line);
//            }
        //}
        //else {
        //    System.out.println("executable ran successfully, output:");
        //    while ((line = execStandardOutBufferedReader.readLine()) != null) {
        //        System.out.println(line);
        //    }
        //}
    }

    // We walk through each node in turn at this level, recursing for any list encountered in any of the Node positions.
    // Once we end up with the evaluated list of nodes, then we evaluate the "flattened" list at this level as a form.
    // We call into our NodeListener for individual atoms as well as the overall form at each level while we still
    // evolve the design.
    void walkTopLevelNodes(List<Node> nodes) throws IOException {
        asmGenerator.initMainFunction();

        for (Node node : nodes) {
            walkTree(node);
        }

        asmGenerator.printResultAndCleanUpMainFunction();

        // TODO user-defined functions added in here
 
        // Generate data segment
//        asmGenerator.generateGlobals();


        BufferedWriter bw = new BufferedWriter(new FileWriter("./src/main/asm/my-asm.s"));

        asmGenerator.dumpAsm(bw);
    }


    public void walkTree(Node node) {
        if (node instanceof Atom atom) {
            handleAtom(atom);
        }
        else if (node instanceof RList rlist) {
            walkTree(rlist);
        }
    }

    private TypedAtom<?> handleAtom(Atom atom) {
        TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
        if (typedAtom instanceof SymbolAtom symbolAtom) {
            asmGenerator.generateSymbolLookup(symbolAtom.getValue(), Namespace.VARIABLE);
        }
        else {
            throw new UnsupportedOperationException("unsupported to eval other types of atoms");

        }
        return typedAtom;
    }

    private void walkTree(RList rlist) {
        if (rlist.nodes().getFirst() instanceof Atom operatorAtom) {
            TypedAtom<?> ta = TypedAtom.fromAtom(operatorAtom);
            if (ta instanceof SymbolAtom sa) {
                Operator op = Operator.fromSymbol(sa.getValue());
                if (op.getOperatorType() == OperatorType.FUNCTION) {
                    walkTreeForFunction(rlist, op);
                }
                else {
                    if (OperatorName.IF.equals(op.getOperatorName())) {
                        new IfSpecialForm().walkTree(rlist, this, asmGenerator);
                    }
                    else if (OperatorName.DEFVAR.equals(op.getOperatorName())) {
                        new DefvarSpecialForm().walkTree(rlist, this, asmGenerator);
                    }
                    else if (OperatorName.DEFUN.equals(op.getOperatorName())) {
                        new DefunSpecialForm().walkTree(rlist, this, asmGenerator);
                    }
                    else {
                        throw new UnsupportedOperationException("unsupported special form %s".formatted(op.getOperatorName()));
                    }
                }
            }
            else {
                throw new UnsupportedOperationException("can't coerce type of operator atom");
            }

        }
        else {
            throw new UnsupportedOperationException("possibly trying to eval a lambda; we're not ready yet");
        }
    }

    private void walkTreeForFunction(RList rlist, Operator operator) {
        // We're evaluating a form.
        // Depending on the operand count, we know how much stack to reserve to hold them.
        int numOperands = rlist.size()-1;

        // We need 16 bytes for each operand plus one for the function pointer; but ensure we always reserve a multiple
        // of 16 bytes
        int stackBytes = (int)(16 * Math.ceil(numOperands+1/2f));
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
                    String symbol = symbolAtom.getValue();
                    if (slot == 0) {
                        // operator position; look up symbol from function namespace.  This will return a function ptr.
                        // we'll then write the function pointer after the operands (stackSlot was already post-incremented on last operand)
                        asmGenerator.generateSymbolLookup(symbol, Namespace.FUNCTION);
                        asmGenerator.untagFunctionPtr();
                        asmGenerator.storeResultToStack(slot++);
                    }
                    else {
                        throw new UnsupportedOperationException("not ready for symbol eval in operand pos quite yet");
                    }
                }
            }
            else if (childNode instanceof RList innerRList) {
                // Processing of the inner form will recursively write assembly like we are here; the result will be in
                // x0 so we write it to the next pos on our stack of evaluated operands for this form.
                walkTree(innerRList);
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
}
