package reader;

import syntaxtree.SyntaxTreeBuilder;

import java.util.Set;

public class CharacterReader {
    private final static Set<Character> QUOTE_CHARS = Set.of(
            '\'', '#', '`', '"'
    );

    private final SyntaxTreeBuilder syntaxTreeBuilder;
    private int depth = 0;

    public CharacterReader(SyntaxTreeBuilder syntaxTreeBuilder) {
        this.syntaxTreeBuilder = syntaxTreeBuilder;
    }

    public void read(String program) {
        for (char c : program.toCharArray()) {
            consume(c);
        }
    }

    public void consume(char c) {
        if (QUOTE_CHARS.contains(c)) {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.ON_QUOTE_CHAR, depth);
            syntaxTreeBuilder.onQuoteChar(event);
        } else if (c == '(') {
            depth++;
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.OPEN_LIST, depth);
            syntaxTreeBuilder.startList(event);
        } else if (c == ')') {
            depth--;
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.CLOSE_LIST, depth);
            syntaxTreeBuilder.endList(event);
        } else if (Character.isWhitespace(c)) {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.SPACE, depth);
            syntaxTreeBuilder.onSpace(event);
        } else {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.IN_ATOM, depth);
            syntaxTreeBuilder.inAtom(event);
        }
    }
}
