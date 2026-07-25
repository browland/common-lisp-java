package treewalker;

import compiler.AsmGenerator;
import compiler.Operator;
import compiler.OperatorName;
import compiler.OperatorType;
import compiler.specialform.IfSpecialForm;
import reader.NodeBuilder;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.io.*;
import java.util.List;

// Beginnings of compiler and might end up being retro-fitted to the existing interpreter as a general case of tree-
// walking.
public class TreeWalker {
    private final BufferedWriter bw = new BufferedWriter(new FileWriter("./src/main/asm/my-asm.s"));
    private final AsmGenerator asmGenerator = new AsmGenerator();

    public TreeWalker() throws IOException {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NodeBuilder nodeBuilder = new NodeBuilder();

        TreeWalker walker = new TreeWalker();

        // String atom
//        String program = "(+ 1 (+ 1 2))";
        String program = "(if nil (+ 1 1) (+ 1 2))";
        List<Node> nodes = nodeBuilder.build(program);
        walker.walkTopLevelNodes(nodes);

        // Assemble
        Process clangProcess = Runtime.getRuntime().exec(new String[] {"clang", "./src/main/asm/my-asm.s", "./src/main/c/runtime.c"});
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
        asmGenerator.initMainFunction(bw);

        for (Node node : nodes) {
            walkTree(node);
        }

        asmGenerator.printResultAndCleanUpMainFunction(bw);

        bw.close();
    }


    public void walkTree(Node node) throws IOException {
        if (node instanceof Atom atom) {
            handleAtom(atom);
        }
        else if (node instanceof RList rlist) {
            walkTree(rlist);
        }
    }

    private TypedAtom<?> handleAtom(Atom atom) throws IOException {
        TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
        if (typedAtom instanceof SymbolAtom symbolAtom) {
            // generate asm to do sym table lookup and the result is what's in the variable (namespace) slot
            asmGenerator.generateSymbolLookup(symbolAtom.getValue(), bw);
        }
        else {
            throw new UnsupportedOperationException("unsupported to eval other types of atoms");

        }
        return typedAtom;
    }

    private void walkTree(RList rlist) throws IOException {
        if (rlist.nodes().getFirst() instanceof Atom operatorAtom) {
            TypedAtom<?> ta = TypedAtom.fromAtom(operatorAtom);
            if (ta instanceof SymbolAtom sa) {
                Operator op = Operator.fromSymbol(sa.getValue());
                if (op.getOperatorType() == OperatorType.FUNCTION) {
                    walkTreeForFunction(rlist, op);
                }
                else {
                    if (OperatorName.IF.equals(op.getOperatorName())) {
                        new IfSpecialForm().walkTree(rlist, this, bw, asmGenerator);
                    }
                    else {
                        throw new UnsupportedOperationException("can only do if special form for now");
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

    private void walkTreeForFunction(RList rlist, Operator operator) throws IOException {
        // We're evaluating a form.
        // Depending on the operand count, we know how much stack to reserve to hold them.
        int numOperands = rlist.size()-1;
        // We need 16 bytes for each 2 operands; ensure we always reserve a multiple of 16 bytes
        int stackBytes = (int)(16 * Math.ceil(numOperands/2f));
        asmGenerator.reserveSpaceOnStack(stackBytes, bw);

        int pos = 0;
        for (Node childNode : rlist.nodes()) {
            if (childNode instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
                if (typedAtom instanceof  IntAtom intAtom) {
                    long fixNum = intAtom.getFixNum();
                    asmGenerator.pushFixNumToStack(pos++, fixNum, bw);
                }
            }
            else if (childNode instanceof RList innerRList) {
                // Processing of the inner form will recursively write assembly like we are here; the result will be in
                // x0 so we write it to the next pos on our stack of evaluated operands for this form.
                walkTree(innerRList);
                asmGenerator.storeResultToStack(pos++, bw);
            }
        }

        // Now the evaluated operands are on the stack, load them into registers ready for our operator call
        for (int i=0; i<numOperands; i++) {
            asmGenerator.loadOperandFromStackIntoRegister(i, bw);
        }

        asmGenerator.callFunction(bw, operator.getOperatorName());
        asmGenerator.freeSpaceOnStack(stackBytes, bw);
    }
}
