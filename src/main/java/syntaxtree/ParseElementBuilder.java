package syntaxtree;

import reader.CharacterReaderEvent;

/**
 * This is more complex than the layer below (CharacterReader).
 * This receives character-level events, then gathers prefixes, and detects the start and end of lists as events are incrementally received
 * from the CharacterReader below.
 * It then fires off higher level events to the layer above - these events represent an entire atom, prefix, or start/end of a list.
 */
public class ParseElementBuilder {
    // The job of this layer is to gather state for a prefix, suffix, or atom into the appropriate builder.
    private final StringBuilder prefixBuilder = new StringBuilder();
    private final StringBuilder suffixBuilder = new StringBuilder();
    private final StringBuilder atomStringBuilder = new StringBuilder();

    private final SyntaxTreeBuilder syntaxTreeBuilder;

    public ParseElementBuilder(SyntaxTreeBuilder syntaxTreeBuilder) {
        this.syntaxTreeBuilder = syntaxTreeBuilder;
    }

    public void inAtom(CharacterReaderEvent event) {
        if(!suffixBuilder.isEmpty()) {
            throw new IllegalArgumentException("Seeing atom characters after suffix quote");
        }
        atomStringBuilder.append(event.character());
    }

    public void onQuoteChar(CharacterReaderEvent event) {
        // Because the atomStringBuilder is only cleared at the end of a list or on whitespace (or upon reset, which
        // is called externally and is a bit of a code-smell - shouldn't need it?) then we can determine if this quote
        // character is a prefix or suffix by whether there are characters present in the atomStringBuilder.
        if(atomStringBuilder.isEmpty()) {
            prefixBuilder.append(event.character());  // we'll still need this when we deal with 2-char quote types
            String prefix = prefixBuilder.toString();;
            // Single quote always results in a (quote ...) form being emitted.
            if(prefix.equals("'")) {  // todo use constant/enum
                handleQuote(QuoteType.QUOTE);
            }
            else if(prefix.equals("#'")) {
                handleQuote(QuoteType.FUNCTION_QUOTE);
            }
            else if(prefix.equals("`")) {
                handleQuote(QuoteType.QUASIQUOTE);
            }
            else if(prefix.equals(",")) {
                handleQuote(QuoteType.UNQUOTE);
            }
        }
        else if(!atomStringBuilder.isEmpty()) {
            suffixBuilder.append(event.character());
        }
    }

    public void onWhitespace(CharacterReaderEvent event) {
        // if we don't have any characters from an atom then by elimination this is a space after the end of a list.
        // We already handled the end of the list (when we saw the close-bracket) so there is nothing we need to do.
        // However, we also need to check for the lack of a prefix, as we may be handling a string (double-quoted) beginning
        // with 1 or more spaces.  This is why we treat double-quote specially (as a quote char) even though it's not
        // a quote char in terms of lisp syntax.  Maybe we could break it out somehow, but for now this works.
        if(atomStringBuilder.isEmpty() && prefixBuilder.isEmpty()) {
            return;
        }

        // If we're within a string literal (seen open-quote but not close-quote) then treat space as just another
        // character for the current atom
        if(!prefixBuilder.isEmpty() && "\"".equals(prefixBuilder.toString()) && suffixBuilder.isEmpty()) {
            atomStringBuilder.append(event.character());
            return;
        }

        // otherwise this space signifies the end of the atom we've been building
        String atom = atomStringBuilder.toString();

        String prefix = prefixBuilder.length() != 0 ? prefixBuilder.toString() : null;
        String suffix = suffixBuilder.length() != 0 ? suffixBuilder.toString() : null;

        Atom.Builder atomBuilder = new Atom.Builder()
                .value(atom)
                .prefix(prefix)
                .suffix(suffix);
        syntaxTreeBuilder.newAtom(atomBuilder);

        atomStringBuilder.delete(0, atomStringBuilder.length());
        prefixBuilder.delete(0, prefixBuilder.length());
        suffixBuilder.delete(0, suffixBuilder.length());
    }

    public void endList(CharacterReaderEvent event) {
        if(!atomStringBuilder.isEmpty()) {
            String prefix = prefixBuilder.length() != 0 ? prefixBuilder.toString() : null;
            String suffix = suffixBuilder.length() != 0 ? suffixBuilder.toString() : null;

            Atom.Builder atomBuilder = new Atom.Builder()
                    .value(atomStringBuilder.toString())
                    .prefix(prefix)
                    .suffix(suffix);
            syntaxTreeBuilder.newAtom(atomBuilder);

            atomStringBuilder.delete(0, atomStringBuilder.length());
        }
        syntaxTreeBuilder.endList();
    }

    public void handleQuote(QuoteType quoteType) {
        syntaxTreeBuilder.insertQuote(quoteType);
        prefixBuilder.delete(0, prefixBuilder.length());  // prefix has now been consumed
    }

    public void startList(CharacterReaderEvent event) {
        // Determine the entire prefix - may be null, 1 or 2 characters based on current knowledge
        String prefix = prefixBuilder.length() != 0 ? prefixBuilder.toString() : null;
        syntaxTreeBuilder.startList(prefix);
        prefixBuilder.delete(0, prefixBuilder.length());  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read
    }

    public void reset() {
        prefixBuilder.delete(0, prefixBuilder.length());
        suffixBuilder.delete(0, suffixBuilder.length());
        atomStringBuilder.delete(0, atomStringBuilder.length());
        syntaxTreeBuilder.reset();
    }
}
