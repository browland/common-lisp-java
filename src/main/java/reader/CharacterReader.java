package reader;

import syntaxtree.ParseElementBuilder;

import java.util.Set;

/**
 * This layer reads character by character from the supplied program, characterises them, tracks depth and
 * pushes each character (with its category and depth) to the layer above.
 */
public class CharacterReader {
    private final static Set<Character> QUOTE_CHARS = Set.of(
            '\'', '#', '`', '"', ':', '&', ','
    );

    private final ParseElementBuilder parseElementBuilder;

    public CharacterReader(ParseElementBuilder parseElementBuilder) {
        this.parseElementBuilder = parseElementBuilder;
    }

    public void read(String program) {
        for (char c : program.toCharArray()) {
            consume(c);
        }
    }

    public void consume(char c) {
        if (QUOTE_CHARS.contains(c)) {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.ON_QUOTE_CHAR);
            parseElementBuilder.onQuoteChar(event);
        } else if (c == '(') {
            parseElementBuilder.startList();
        } else if (c == ')') {
            parseElementBuilder.endList();
        } else if (Character.isWhitespace(c)) {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.SPACE);
            parseElementBuilder.onWhitespace(event);
        } else {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.IN_ATOM);
            parseElementBuilder.inAtom(event);
        }
    }
}
