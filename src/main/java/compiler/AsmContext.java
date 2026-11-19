package compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsmContext {
    // 0-based, i.e. instruction part occupies indices 0 to 49, and comment starts index 50
    private final int COMMENT_INDEX = 50;

    private List<String> lines = new ArrayList<>();

    public void write(String asm) {
        asm.lines().forEach(line -> {
            // If line contains inline comment which starts later than position 0, then justify it for readability
            int commentIndex = line.indexOf(';');
            if (commentIndex > 0) {
                String instructionPart = line.substring(0, commentIndex).stripTrailing();

                if (instructionPart.length() > COMMENT_INDEX) {
                    throw new RuntimeException("Unexpectedly long instruction prior to comment");
                }
                int remainingChars = COMMENT_INDEX - instructionPart.length();
                String paddedInstructionPart = instructionPart + " ".repeat(remainingChars);
                line = paddedInstructionPart + line.substring(commentIndex);
            }
            lines.add(line);
        });
    }

    public void dumpAsm(BufferedWriter bw) throws IOException {
        for (String line : lines) {
//            bw.write(".p2align 3\n");
            bw.write(line + "\n");
        }
    }
}
