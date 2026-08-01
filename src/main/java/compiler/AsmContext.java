package compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsmContext {
    private List<String> lines = new ArrayList<>();

    public void write(String asm) {
        asm.lines().forEach(line -> lines.add(line));
    }

    public void dumpAsm(BufferedWriter bw) throws IOException {
        for (String line : lines) {
            bw.write(line + "\n");
        }
    }
}
