package compiler

import compiler.treewalker.TreeWalker
import reader.NodeBuilder
import spock.lang.Specification
import syntaxtree.Node

class CompilerSpec extends Specification {

    def "failure tests"() {
        when:
        compile(program)

        then:
        thrown(IllegalArgumentException)

        where:
        program || _
        "(defvar x 1) (defvar x 2)"                        || _
        "(setq x 1)"                                       || _
        "(block (+ 1 1))"                                  || _
        "(block 1)"                                        || _
        "(defun foo () (go start)) (tagbody start (foo))"  || _
        // TODO add a failure-mode test for a 'go' issued from within a closure which is evaluated after the tagbody has exited
        //      (probably needs funcall to be impl'd).
    }

    def "compilation tests"() {
        when:
        compile(program)

        then:
        run() ==  expectedResult

        where:
        program                                              || expectedResult
        "2"                                                  || "2"
        "(oneplus 1)"                                        || "2"
        "nil"                                                || "nil"
        "t"                                                  || "t"
        "(add 1 2)"                                          || "3"
        "(+ 1 2)"                                            || "3"
        "(+ 1 (+ 1 2))"                                      || "4"
        "(defvar two (+ 1 1)) (if t two (+ 1 2))"            || "2"
        "(defvar x 2) (if nil nil x)"                        || "2"
        "(let ((x 1)) (+ x 1))"                              || "2"
        "(let ((x 1)) (let ((y 2)) (+ x y)))"                || "3"
        "(defvar x 1) (setq x 2) x"                          || "2"
        "(let ((x 1)) (setq x 2) x)"                         || "2"
        "\"hello world\""                                    || "\"hello world\""

    }

    def "conditionals tests"() {
        when:
        compile(program)

        then:
        run() ==  expectedResult

        where:
        program                                              || expectedResult
        "(if t 1 2)"                                         || "1"
        "(if nil 1 2)"                                       || "2"
        "(+ 1 (if t 1 2))"                                   || "2"
    }

    def "cons and list tests"() {
        when:
        compile(program)

        then:
        run() ==  expectedResult

        where:
        program                                              || expectedResult
        "(cons 1 2)"                                         || "(1 . 2)"
        "(cons 1 (cons 1 2))"                                || "(1 . (1 . 2))"
        "(cons (+ 1 2) (+ 2 1))"                             || "(3 . 3)"
        "(list 1 2)"                                         || "(1 . (2 . nil))"
        "(list 1 2 3)"                                       || "(1 . (2 . (3 . nil)))"
        "(let ((x 1) (y 2)) (list x y))"                     || "(1 . (2 . nil))"
        "(cdr (list 1 2 3))"                                 || "(2 . (3 . nil))"
        "(mapcar (function oneplus) (list 1 2))"             || "(2 . (3 . nil))"
        "(mapcar #'oneplus (list 1 2))"                      || "(2 . (3 . nil))"
    }

    def "lambda tests"() {
        when:
        compile(program)

        then:
        checkContains ? run().contains(expectedResult) : run() == expectedResult

        where:
        program                                              || expectedResult     || checkContains
        "(lambda (x) (+ x 1))"                               || "(lambda (x) ())"  || true
        "((lambda (x) (+ x 1)) 1)"                           || "2"                || false
        "((lambda (x y) (+ x y)) 1 2)"                       || "3"                || false
        "(let ((x 1)) ((lambda (y) (+ x y)) 2))"             || "3"                || false
        "(let ((x 1) (y 2)) ((lambda () (+ x y))))"          || "3"                || false
        "(defvar x 1) ((lambda (y) (+ y x)) 2)"              || "3"                || false
    }

    def "function tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program || expectedResult
        "(defun foo () 2) (foo)"                             || "2"
        "(defun foo () (add 1 1)) (if t (foo) (add 1 2))"    || "2"
        "(defun first (x y) x) (first 1 2)"                  || "1"
        "(defun adder (x y) (+ x y)) (adder 1 2)"            || "3"
        "(function add)"                                     || "#<FUNCTION add>"
    }

    def "block tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program || expectedResult
        "(block foo (return-from foo))"                      || "nil"
        "(block foo (return-from foo 1))"                    || "1"
        "(block nil (return-from nil 1))"                    || "1"
    }

    def "tagbody tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program || expectedResult
        "(let ((x 1)) (tagbody (go end) (setq x 2) end) x)"                                       || "1"
        "(let ((x 1)) (tagbody (go end) (setq x (+ x 1)) end (setq x (+ x 2))) x)"                || "3"
        "(let ((i 0)) (tagbody start (if (eq i 3) (go end) (setq i (+ i 1))) (go start) end) i)"  || "3"
        "(tagbody start (+ 1 (go end)) end)"                                                      || "nil"
    }

    def "eq tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program || expectedResult
        "(eq 1 1) "                                       || "t"
        "(eq 1 2) "                                       || "nil"
        "(eq (cons 1 2) (cons 2 1)) "                     || "nil"
        "(let ((x (cons 1 2))) (eq x x))"                 || "t"
        "(let ((x (cons 1 2)) (y (cons 1 2))) (eq x y))"  || "nil"
    }

    def "block and tagbody tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program || expectedResult
        "(block foo (tagbody start (return-from foo 7)))"  || "7"
    }

    def "quote tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program              || expectedResult
        "'nil"               || "nil"
        "'1"                 || "1"
        "'(1 2 3)"           || "(1 . (2 . (3 . nil)))"
        "(defvar x 1) '(x)"  || "(x . nil)"
        "'(x)"               || "(x . nil)"
    }

    def "incf tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program                                              || expectedResult
        "(defvar x 1) (incf x) x"                            || "2"
        "(let ((x 1)) (incf x) x)"                           || "2"
    }

    def "macro tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program                                              || expectedResult
        "(defmacro testing (x y) `(+ ,x ,y)) (testing 1 2)"  || "3"
    }

    def "keyword tests"() {
        when:
        compile(program)

        then:
        run() == expectedResult

        where:
        program                                               || expectedResult
        ":foo"                                                || ":foo"
        "(eq :foo :foo)"                                      || "t"
        "(defun checker (sym) (eq sym :foo)) (checker :foo)"  || "t"
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
