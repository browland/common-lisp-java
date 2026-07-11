package compiler;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * V. basic test for generating an executable from generated assembly
 */
public class BasicAsmTest {
    public static void main(String[] args) throws IOException, InterruptedException {
        String myAsm = """
                    .text
                    .global _main
                    .p2align 3
                _main:
                    mov x0, #0
                    ret
                """;

        File asmFile = new File("./my-asm.s");
        try (FileWriter fw = new FileWriter(asmFile)) {
            fw.write(myAsm);
        }

        Process p1 = Runtime.getRuntime().exec(new String[] {"clang", "./my-asm.s"});
        int clangExitCode = p1.waitFor();
        if (clangExitCode != 0) {
            System.out.println("assemble step failed, exit code: " + clangExitCode);
        }
        else {
            System.out.println("assemble step successful");
        }

        Process p2 = Runtime.getRuntime().exec(new String[] {"./a.out"});
        int executableExitCode = p2.waitFor();
        if (executableExitCode != 0) {
            System.out.println("built executable failed, exit code: " + executableExitCode);
        }
        else {
            System.out.println("built executable ran successfully; exit code was " + executableExitCode);
        }
    }
}
