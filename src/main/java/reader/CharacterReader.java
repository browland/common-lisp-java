package reader;

import syntaxtree.ParseElementBuilder;

import java.util.Set;

/**
 * This layer reads character by character from the supplied program, characterises them, tracks depth and
 * pushes each character (with its category and depth) to the layer above.
 */
public class CharacterReader {
    private final static Set<Character> QUOTE_CHARS = Set.of(
            '\'', '#', '`', ',', '@'
    );

    private final ParseElementBuilder parseElementBuilder;

    public CharacterReader(ParseElementBuilder parseElementBuilder) {
        this.parseElementBuilder = parseElementBuilder;
    }

    public void read(String program) {
        for (char c : program.toCharArray()) {
            consume(c);
        }

        // Handle case for evaluating a single atom (parseElementBuilder.atomStringBuilder is not yet consumed)
        parseElementBuilder.onEndProgram();
    }

    /**
     * Returns true if we should skip the rest of the current line, otherwise false.
     */
    public void consume(char c) {
        // handle beginning of comment
        if(c == ';') {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.COMMENT_START);
            parseElementBuilder.onCommentSymbol(event);
        } else if (Character.isWhitespace(c)) {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.WHITESPACE);
            parseElementBuilder.onWhitespace(event);
        }
        else if (QUOTE_CHARS.contains(c)) {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.ON_QUOTE_CHAR);
            parseElementBuilder.onQuoteChar(event);
        } else if (c == '(') {
            parseElementBuilder.startList();
        } else if (c == ')') {
            parseElementBuilder.endList();
        } else {
            CharacterReaderEvent event = new CharacterReaderEvent(c, CharacterType.IN_ATOM);
            parseElementBuilder.inAtom(event);
        }
    }
}
