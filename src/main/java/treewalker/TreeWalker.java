package treewalker;

import reader.NodeBuilder;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

// Beginnings of compiler and might end up being retro-fitted to the existing interpreter as a general case of tree-
// walking.
public class TreeWalker {
    private final BufferedWriter bw = new BufferedWriter(new FileWriter(new File("my-asm.s")));

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

        // Run
        Process p2 = Runtime.getRuntime().exec(new String[] {"./a.out"});
        int executableExitCode = p2.waitFor();
        if (executableExitCode != 0) {
            System.out.println("built executable failed, exit code: " + executableExitCode);
        }
        else {
            System.out.println("built executable ran successfully; exit code was " + executableExitCode);
        }
    }

    // We walk through each node in turn at this level, recursing for any list encountered in any of the Node positions.
    // Once we end up with the evaluated list of nodes, then we evaluate the "flattened" list at this level as a form.
    // We call into our NodeListener for individual atoms as well as the overall form at each level while we still
    // evolve the design.
    private void walkTopLevelNodes(List<Node> nodes) throws IOException {
        bw.write(
        """
.text
.global _main
_main:
.p2align 3
  stp x29, x30, [sp, #-16]!  ;; allocate stack and store frame pointer and link register
  mov x29, sp                ;; set frame pointer
""");

        for (Node node : nodes) {
            walkTree(node);
        }
        bw.write("""
  bl _printResult
  ldp x29, x30, [x29]
  add sp, sp, #16
  ret
""");
        generateAddAsm();
        generatePrintResultAsm();
        generateGlobals();
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
        bw.write("""
    sub sp, sp, #%d           ;; reserve space for operands
  """.formatted(stackBytes));

        int pos = 0;
        for (Node childNode : rlist.nodes()) {
            if (childNode instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
                if (typedAtom instanceof  IntAtom intAtom) {
                    long fixNum = intAtom.getFixNum();
                    bw.write("""
    mov x0, #%d          ;; move operand (fixnum) to x0
    str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
  """.formatted(fixNum, (pos++)*8));
                }
            }
            else if (childNode instanceof RList innerRList) {
                // Processing of the inner form will recursively write assembly like we are here; the result will be in
                // x0 so we write it to the next pos on our stack of evaluated operands for this form.
                walkTree(innerRList);
                bw.write("""
  str x0, [sp, #%d]  ;; store fixnum on stack to free x0 for further operand processing
""".formatted((pos++)*8));
            }
        }

        // Now the evaluated operands are on the stack, load them into registers ready for our operator call
        for (int i=0; i<numOperands; i++) {
            bw.write("""
      ldr x%d, [sp, #%d]   ;; load evaluated operand into register ready for operator call
    """.formatted(i, i*8));
        }

        bw.write("""
      bl _add                ;; call operator; this leaves the result in x0 for our caller
      add sp, sp, #%d
    """.formatted(stackBytes));
    }

    private void generateAddAsm() throws IOException {
        bw.write("""

.global _add
_add:
  mov x2, #0x7    ; bit mask to select out the low 3 bits only
  and x3, x0, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, _no_match
            
  and x3, x1, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, _no_match
_match:
  lsr x0, x0, #3  ; logical shift right x0 to remove type tag bits
  lsr x1, x1, #3  ; logical shift right x1 to remove type tag bits
  add x0, x0, x1
                
  lsl x0, x0, #3
  orr x0, x0, #0x1
  ret
_no_match:  ;; no match
  adrp x0, _error_msg@PAGE
  add x0, x0, _error_msg@PAGEOFF
  bl _printf
  bl _exit
""");
    }

    // MacOS has a weird thing where making a call to printf requires the numeric argument to be on the stack at [sp]
    // while the string pointer is in x0 as usual.
    private void generatePrintResultAsm() throws IOException {
        bw.write("""
.global _printResult
_printResult:
  mov x2, #0x7    ; bit mask to select out the low 3 bits only
  and x3, x0, x2  ; store result of masking x0 in x3
  sub x3, x3, #1  ; check that the masked bits are equal to 1
  cbnz x3, _pR_no_match
_pR_match:
  stp x29, x30, [sp, #-16]!  ;; allocate stack and store frame pointer and link register
  lsr x0, x0, #3  ; logical shift right x0 to remove type tag bits
  sub sp, sp, #16
  str x0, [sp]
  adrp x0, _printed_result@PAGE
  add x0, x0, _printed_result@PAGEOFF
  bl _printf
  add sp, sp, #16
  ldp x29, x30, [sp], #16
  ret
  
_pR_no_match:  ;; no match
  adrp x0, _error_msg@PAGE
  add x0, x0, _error_msg@PAGEOFF
  bl _printf
  bl _exit
""");
    }

    void generateGlobals() throws IOException {
        // Static strings for things like messages
        bw.write("""

.cstring:
_error_msg:
  .asciz \"ERROR (incorrect value type)\\n\"
_printed_result:
  .asciz \"result: %d\\n\"
""");
    }
}
