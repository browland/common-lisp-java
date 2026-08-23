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
    private CompilerBackend backend = new CompilerBackend();
    private final Map<String, SpecialForm> specialForms = new HashMap<>();
    private final Map<String, Function> functions = new HashMap<>();

    public TreeWalker() {
        specialForms.put("if", new IfSpecialForm());
        specialForms.put("defun", new DefunSpecialForm());
        specialForms.put("defvar", new DefvarSpecialForm());
        specialForms.put("lambda", new LambdaSpecialForm());
        specialForms.put("let", new LetSpecialForm());

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
//        String program = "(defun first (x y) x) (first 1 2)";
//        String program = "(defvar two (+ 1 1)) (if t two (+ 1 2))";
//        String program = "(defvar x 2) (if nil nil x)";
//        String program = "(defun adder (x y) (+ x y)) (adder 1 2)";
//        String program = "((lambda (x) (+ x 1)) 1)";
//        String program = "((lambda (x y) (+ x y)) 1 2)";
//        String program = "(let ((x 1)) (+ x 1))";
//        String program = "(let ((x 1)) (let ((y 2)) (+ x y)))";

        // TODO Escape analysis: x in the lambda body is free; we expect it to be in the symbol table but it's in the enclosing scope
//        String program = "(let ((x 1)) ((lambda (y) (+ x y)) 2))";

        // Multiple captures
        String program = "(let ((x 1) (y 2)) ((lambda () (+ x y))))";

        // attempt to reproduce issue where lambda accesses var in surrounding scope (not in symbol table)
        // (defun foo (x) (lambda (y) x))
        // we'd then need to call it like this though:
        // (+ (funcall (foo 2) 1) 1)

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
    void walkTopLevelNodes(List<Node> nodes) {
        backend.startProgram();

        for (Node node : nodes) {
            walkTree(node);
        }

        backend.endProgram();
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
            String symbol = symbolAtom.getValue();
            backend.handleSymbolOperand(symbol, Namespace.VARIABLE);
        }
        else if (typedAtom instanceof IntAtom intAtom) {
            // Operand index 0 since this is a bare atom
            backend.handleIntOperand(intAtom, 0);
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
                handleFormWithSymbolOperator(rlist, sa);
            }
            else {
                throw new UnsupportedOperationException("can't coerce type of operator atom");
            }

        }
        else if (rlist.nodes().getFirst() instanceof RList expectedLambda) {
            handleFormWithLambdaOperator(rlist, expectedLambda);
        }
    }

    private void handleFormWithSymbolOperator(RList rlist, SymbolAtom sa) {
        System.out.println("Handling form with symbol operator");

        // First check operator symbol for match on special forms which are statically defined.
        SpecialForm specialForm = specialForms.get(sa.getValue());
        if (specialForm != null) {
            System.out.println("Found special form for symbol " + sa.getValue());
            specialForm.walkTree(rlist, this, backend);
        }
        else {
            Function function = backend.findFunction(sa.getValue());
            if (function != null) {
                System.out.println("Found function for symbol " + sa.getValue());
                walkTreeForFunctionCall(rlist, function);
            }
            else {
                throw new UnsupportedOperationException("Could not find symbol for operator " + sa.getValue());
            }
        }
    }

    private void handleFormWithLambdaOperator(RList rlist, RList expectedLambda) {
        System.out.println("Handling form with lambda operator");

        if (expectedLambda.nodes().getFirst() instanceof Atom lambdaAtom) {
            if ("lambda".equals(lambdaAtom.value())) {
                walkTreeForLambdaApply(rlist);
            }
            else {
                throw new IllegalArgumentException("Invalid operator, not a lambda expression: " + expectedLambda);
            }
        }
        else {
            throw new IllegalArgumentException("Invalid operator, not a lambda expression: " + expectedLambda);
        }
    }

    private void walkTreeForFunctionCall(RList rlist, Function functionToCall) {
        // We're evaluating a form.
        // Depending on the operand count, we know how much stack to reserve to hold them.
        int numOperands = rlist.size()-1;

        // Reserve stack for operands plus function pointer for our the operator being applied
        backend.reserveStackForVariables(numOperands + 1);

        // This doubles as the stack slot index, and also the node index of the list we're walking
        int slot = 0;

        for (Node childNode : rlist.nodes()) {
            if (childNode instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
                if (typedAtom instanceof  IntAtom intAtom) {
                    backend.handleIntOperand(intAtom, slot);
                    backend.storeResultToVariable(slot);
                    slot++;
                }
                else if (typedAtom instanceof SymbolAtom symbolAtom) {
                    if (slot == 0) {
                        // operator position; look up symbol from function namespace.  This will return a function ptr.
                        // we'll then write the function pointer after the operands (stackSlot was already post-incremented on last operand)
                        backend.handleSymbolOperand(functionToCall.getSymbolStringName(), Namespace.FUNCTION);
                        backend.untagFunctionPointer();
                        backend.storeResultToVariable(slot++);
                    }
                    else {
                        String symbol = symbolAtom.getValue();
                        backend.handleSymbolOperand(symbol, Namespace.VARIABLE);
                        backend.storeResultToVariable(slot++);
                    }
                }
            }
            else if (childNode instanceof RList innerRList) {
                // We have a form to evaluate.
                // Processing of the inner form will recursively write assembly like we are here; the result will be in
                // x0 so we write it to the next pos on our stack of evaluated operands for this form.
                walkTree(innerRList);
                backend.storeResultToVariable(slot++);
            }
        }

        // TODO now we have the tagged function ptr on the stack, determine if it points to a closure (in heap) or a plain static function (directly label in asm).
        //      So check the tag and if it's a closure generate the code to move captured variables which were stored on heap to next stack positions.
        //      Replace the tagged function ptr on stack with the tagged ptr to the static code.
        //      1. if tagged fxn ptr last 3 bits are 0x3, then:
        //      2. untag the fxn ptr and deref it - this gives us the closure struct containing: tagged real fxn ptr, N slots for captured variables (we know how many slots as our functionToCall contains the capturedSymbols).
        //      3. We load each captured variable onto the stack after the existing bindings
        //      4. Then put tagged real fxn ptr on the stack in appropriate place

        // Now the evaluated operands are on the stack, load them into registers ready for our operator call
        // Operands will be stored in registers in incrementing order as per usual calling convention
        for (int operandNum = 0; operandNum<numOperands; operandNum++) {
            // Zero-indexed operands are aligned with registers, but stack pos is one pos higher (above the function ptr slot)
            backend.loadVariableFromStackIntoRegister(operandNum+1, operandNum);
        }

        // Load function ptr into next available register
        // We use operandNum as it's already incremented to next register num (it's not an operand but we need it in
        // some register for the jump).
        int functionPtrRegister = numOperands; // Function ptr will be stored in the next register after those used by the operands
        backend.loadVariableFromStackIntoRegister(0, functionPtrRegister);

        backend.callFunction(functionPtrRegister);

        // Free space on stack for operands plus function pointer for our the operator being applied
        backend.freeStackForVariables(numOperands + 1);
    }

    private void walkTreeForLambdaApply(RList rlist) {
        // We're evaluating a form.
        // Depending on the operand count, we know how much stack to reserve to hold them.
        int numOperands = rlist.size()-1;

        // Reserve stack space for each operand plus one for the function pointer
        backend.reserveStackForVariables(numOperands + 1);

        // This doubles as the stack slot index, and also the node index of the list we're walking
        int slot = 0;

        for (Node childNode : rlist.nodes()) {
            if (childNode instanceof Atom atom) {
                TypedAtom<?> typedAtom = TypedAtom.fromAtom(atom);
                if (typedAtom instanceof  IntAtom intAtom) {
                    backend.handleIntOperand(intAtom, slot);
                    backend.storeResultToVariable(slot);
                    slot++;
                }
                else if (typedAtom instanceof SymbolAtom symbolAtom) {
                    String symbol = symbolAtom.getValue();
                    backend.handleSymbolOperand(symbol, Namespace.VARIABLE);
                    backend.storeResultToVariable(slot++);
                }
            }
            else if (childNode instanceof RList innerRList) {
                // We have a form to evaluate.
                // Processing of the inner form will recursively write assembly like we are here; the result will be in
                // x0 so we write it to the next pos on our stack of evaluated operands for this form.
                walkTree(innerRList);
                if (slot == 0) {
                    // We saw a list and we're in position 0 so we must have just evaluated a lambda.
                    // We should have a tagged pointer for a closure, which when untagged will point to our Closure struct on the heap.

                    backend.storeResultToVariable(slot++);

                    // We have a tagged closure ptr, and we want the untagged raw fxn ptr.
                    backend.loadRealFunctionPointer();

                    // The tagged pointer will be the return value of the last generated instruction so we can  push that to the stack as we normally would for a function lookup.
                }
                backend.storeResultToVariable(slot++);
            }
        }

        // Now the evaluated operands are on the stack, load them into registers ready for our operator call
        // Operands will be stored in registers in incrementing order as per usual calling convention
        for (int operandNum = 0; operandNum<numOperands; operandNum++) {
            // Zero-indexed operands are aligned with registers, but stack pos is two pos higher (above the function ptr and captures ptr slots)
            backend.loadVariableFromStackIntoRegister(operandNum+2, operandNum);
        }

        // Load closure ptr into next register
        backend.loadVariableFromStackIntoRegister(0, numOperands);

        // Load function ptr into next register
        int functionPtrRegister = numOperands+1; // Function ptr will be stored in the next register after those used by the operands
        backend.loadVariableFromStackIntoRegister(1, functionPtrRegister);

        backend.callFunction(functionPtrRegister);

        // Free stack space for each operand plus one for the function pointer
        backend.freeStackForVariables(numOperands + 1);
    }
}
