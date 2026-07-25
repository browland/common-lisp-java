package compiler;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AsmContext {
//    private List<StringLiteral> stringLiterals = new ArrayList<>();
    private List<String> taggedSymbolNames = new ArrayList<>();
    private List<String> lines = new ArrayList<>();

    public void write(String asm) {
        asm.lines().forEach(line -> lines.add(line));
    }

//    void addStringLiteral(StringLiteral stringLiteral) {
//        stringLiterals.add(stringLiteral);
//    }

    void addTaggedSymbolName(String taggedSymbolName) {
        taggedSymbolNames.add(taggedSymbolName);
    }


//    List<StringLiteral> getStringLiterals() {
//        return stringLiterals;
//    }

    public List<String> getTaggedSymbolNames() {
        return new ArrayList<>(taggedSymbolNames);
    }

    public void dumpAsm(BufferedWriter bw) throws IOException {
        for (String line : lines) {
            bw.write(line + "\n");
        }
        bw.close();
    }
}
