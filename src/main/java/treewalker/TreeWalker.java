package treewalker;

import compiler.AsmGenerator;
import reader.NodeBuilder;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.io.*;
import java.util.List;

// Beginnings of compiler and might end up being retro-fitted to the existing interpreter as a general case of tree-
// walking.
public class TreeWalker {
    private final BufferedWriter bw = new BufferedWriter(new FileWriter(new File("my-asm.s")));
    private final AsmGenerator asmGenerator = new AsmGenerator();

    public TreeWalker() throws IOException {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NodeBuilder nodeBuilder = new NodeBuilder();

        TreeWalker walker = new TreeWalker();

        // String atom
        String program = "(+ 1 (+ 1 2))";
        List<Node> nodes = nodeBuilder.build(program);
        walker.walkTopLevelNodes(nodes);

        // Assemble
        Process p1 = Runtime.getRuntime().exec(new String[] {"clang", "./my-asm.s"});
        int clangExitCode = p1.waitFor();
        if (clangExitCode != 0) {
            System.out.println("assemble step failed, exit code: " + clangExitCode);
        }
        else {
            System.out.println("assemble step successful");
        }

        // Run and print result
        Process p2 = Runtime.getRuntime().exec(new String[] {"./a.out"});
        InputStream is = p2.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        System.out.println(br.readLine());
    }

    // We walk through each node in turn at this level, recursing for any list encountered in any of the Node positions.
    // Once we end up with the evaluated list of nodes, then we evaluate the "flattened" list at this level as a form.
    // We call into our NodeListener for individual atoms as well as the overall form at each level while we still
    // evolve the design.
    private void walkTopLevelNodes(List<Node> nodes) throws IOException {
        asmGenerator.initMainFunction(bw);

        for (Node node : nodes) {
            walkTree(node);
        }

        asmGenerator.printResultAndCleanUpMainFunction(bw);
        asmGenerator.generateAddAsm(bw);
        asmGenerator.generatePrintResultAsm(bw);
        asmGenerator.generateGlobals(bw);

        bw.close();
    }


    private void walkTree(Node node) throws IOException {
        if (node instanceof Atom atom) {
            handleAtom(atom);
        }
        else if (node instanceof RList rlist) {
            walkTree(rlist);
        }
    }

    // TODO unused currently
    private TypedAtom<?> handleAtom(Atom atom) {
        TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
        return typedAtom;
    }

    private void walkTree(RList rlist) throws IOException {
        // We're evaluating a form.
        // Prepare FP and LR and reserve enough stack for our operands
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

        asmGenerator.callFunction(bw);
        asmGenerator.freeSpaceOnStack(stackBytes, bw);
    }
}
