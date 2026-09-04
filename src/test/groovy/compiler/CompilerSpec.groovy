package compiler

import compiler.treewalker.TreeWalker
import reader.NodeBuilder
import spock.lang.Specification
import syntaxtree.Node

class CompilerSpec extends Specification {

    def "compilation tests"() {
        when:
        compile(program)

        then:
        run() ==  expectedResult

        where:
        program                                              || expectedResult
        "2"                                                  || "2"
        "(if t 1 2)"                                         || "1"
        "(add 1 2)"                                          || "3"
        "(+ 1 2)"                                            || "3"
        "(+ 1 (+ 1 2))"                                      || "4"
        "(defun foo () 2) (foo)"                             || "2"
        "(defun foo () (add 1 1)) (if t (foo) (add 1 2))"    || "2"
        "(defun first (x y) x) (first 1 2)"                  || "1"
        "(defvar two (+ 1 1)) (if t two (+ 1 2))"            || "2"
        "(defvar x 2) (if nil nil x)"                        || "2"
        "(defun adder (x y) (+ x y)) (adder 1 2)"            || "3"
        "((lambda (x) (+ x 1)) 1)"                           || "2"
        "((lambda (x y) (+ x y)) 1 2)"                       || "3"
        "(let ((x 1)) (+ x 1))"                              || "2"
        "(let ((x 1)) (let ((y 2)) (+ x y)))"                || "3"
        "(let ((x 1)) ((lambda (y) (+ x y)) 2))"             || "3"
        "(let ((x 1) (y 2)) ((lambda () (+ x y))))"          || "3"
        "(cons 1 2)"                                         || "(1 . 2)"
    }

    def compile(String program) {
        NodeBuilder nodeBuilder = new NodeBuilder();
        List<Node> nodes = nodeBuilder.build(program)
        TreeWalker walker = new TreeWalker();
        walker.walkTopLevelNodes(nodes)

        // Assemble
        // We use -falign-functions=8 to ensure we can use pointer tagging for our built-in functions.  We could mark
        // each function with __attribute__((aligned(8))) but this seems a cleaner option and less likely to forget
        // a case.  At the cost of a larger executable due to the extra padding needed.
        Process clangProcess = Runtime.getRuntime().exec(new String[] {"clang", "-falign-functions=8", "./src/main/asm/my-asm.s", "./src/main/c/runtime.c"})
        InputStream clangStandardError = clangProcess.getErrorStream()
        InputStreamReader clangStandardErrorReader = new InputStreamReader(clangStandardError)
        BufferedReader clangStandardErrorBufferedReader = new BufferedReader(clangStandardErrorReader)

        String line
        while ((line = clangStandardErrorBufferedReader.readLine()) != null) {
            System.out.println(line)
        }

        int clangExitCode = clangProcess.waitFor()
        if (clangExitCode != 0) {
            System.out.println("assemble step failed, exit code: " + clangExitCode)
            System.exit(0)
        }
        else {
            System.out.println("assemble step successful")
        }
    }

    def run() {
        Process execProcess = Runtime.getRuntime().exec(new String[] {"./a.out"})
        InputStream stdoutInputStream = execProcess.getInputStream()
        InputStreamReader inputStreamReader = new InputStreamReader(stdoutInputStream)

        return inputStreamReader.readLine()
    }
}
