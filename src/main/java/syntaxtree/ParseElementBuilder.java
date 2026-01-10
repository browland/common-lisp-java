package syntaxtree;

import reader.CharacterReaderEvent;

import java.util.Optional;

/**
 * This is more complex than the layer below (CharacterReader).
 * This receives character-level events, then gathers prefixes, and detects the start and end of lists as events are incrementally received
 * from the CharacterReader below.
 * It then fires off higher level events to the layer above - these events represent an entire atom, prefix, or start/end of a list.
 */
public class ParseElementBuilder {
    // The job of this layer is to gather state for a prefix, suffix, or atom into the appropriate builder.
    private final StringBuilder prefixBuilder = new StringBuilder();
    private final StringBuilder atomStringBuilder = new StringBuilder();
    private final SyntaxTreeBuilder syntaxTreeBuilder;

    private boolean inString = false;
    private boolean inComment = false;

    public ParseElementBuilder(SyntaxTreeBuilder syntaxTreeBuilder) {
        this.syntaxTreeBuilder = syntaxTreeBuilder;
    }

    public void inAtom(CharacterReaderEvent event) {
        if(inComment) {
            return;
        }

        char character = event.character();
        atomStringBuilder.append(character);

        if(character == '"') {
            inString = !inString;  // flip the boolean so we detect start/end of strings
        }
    }

    public void onQuoteChar(CharacterReaderEvent event) {
        // Because the atomStringBuilder is only cleared at the end of a list or on whitespace (or upon reset, which
        // is called externally and is a bit of a code-smell - shouldn't need it?) then we can determine if this quote
        // character is a prefix or suffix by whether there are characters present in the atomStringBuilder.
        if(atomStringBuilder.isEmpty()) {
            prefixBuilder.append(event.character());  // we'll still need this when we deal with 2-char quote types
            String prefix = prefixBuilder.toString();
            // Single quote always results in a (quote ...) form being emitted.
            Optional<QuoteType> optionalQuoteType = QuoteType.ofPrefix(prefix);
            optionalQuoteType.ifPresent(this::handleQuote);
        }
    }

    public void onWhitespace(CharacterReaderEvent event) {
        // if we don't have any characters from an atom then by elimination this is a space after the end of a list.
        // We already handled the end of the list (when we saw the close-bracket) so there is nothing we need to do.
        // However, we also need to check for the lack of a prefix, as we may be handling a string (double-quoted) beginning
        // with 1 or more spaces.  This is why we treat double-quote specially (as a quote char) even though it's not
        // a quote char in terms of lisp syntax.  Maybe we could break it out somehow, but for now this works.
        // commenting resets at line endings
        if(event.character() == '\n' && inComment) {
            inComment = false;
        }

        if(atomStringBuilder.isEmpty() && prefixBuilder.isEmpty()) {
            return;
        }

        // If we're within a string literal (seen open-quote but not close-quote) then treat space as just another
        // character for the current atom
        if(inString) {
            atomStringBuilder.append(event.character());
            return;
        }

        // otherwise this space signifies the end of the atom we've been building
        String atom = atomStringBuilder.toString();

        String prefix = !prefixBuilder.isEmpty() ? prefixBuilder.toString() : null;
        Atom.Builder atomBuilder = new Atom.Builder()
                .value(atom)
                .prefix(prefix);
        syntaxTreeBuilder.newAtom(atomBuilder);

        atomStringBuilder.delete(0, atomStringBuilder.length());
        prefixBuilder.delete(0, prefixBuilder.length());
    }

    public void onCommentSymbol(CharacterReaderEvent event) {
        if(!inString) {
            inComment = true;
        }
        else {
            inAtom(event);
        }
    }

    public void endList() {
        if(!atomStringBuilder.isEmpty()) {
            String prefix = !prefixBuilder.isEmpty() ? prefixBuilder.toString() : null;

            Atom.Builder atomBuilder = new Atom.Builder()
                    .value(atomStringBuilder.toString())
                    .prefix(prefix);
            syntaxTreeBuilder.newAtom(atomBuilder);

            atomStringBuilder.delete(0, atomStringBuilder.length());
        }
        syntaxTreeBuilder.endList();
    }

    public void startList() {
        // Determine the entire prefix - may be null, 1 or 2 characters based on current knowledge
        String prefix = !prefixBuilder.isEmpty() ? prefixBuilder.toString() : null;
        syntaxTreeBuilder.startList(prefix);
        prefixBuilder.delete(0, prefixBuilder.length());  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read
    }

    public void reset() {
        prefixBuilder.delete(0, prefixBuilder.length());
        atomStringBuilder.delete(0, atomStringBuilder.length());
        syntaxTreeBuilder.reset();
    }

    private void handleQuote(QuoteType quoteType) {
        syntaxTreeBuilder.insertQuote(quoteType);
        prefixBuilder.delete(0, prefixBuilder.length());  // prefix has now been consumed
    }
}
