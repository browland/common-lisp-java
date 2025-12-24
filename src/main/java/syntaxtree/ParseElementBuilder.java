package syntaxtree;

import reader.CharacterReaderEvent;

/**
 * This is more complex than the layer below (CharacterReader).
 * This receives character-level events and builds complete atoms and lists incrementally as events are received.
 * For this reason, it maintains a fair bit of state.
 */
public class ParseElementBuilder {
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
        if(atomStringBuilder.isEmpty()) {
            prefixBuilder.append(event.character());
        }
        else if(!atomStringBuilder.isEmpty()) {
            suffixBuilder.append(event.character());
        }
    }

    public void onWhitespace(CharacterReaderEvent event) {
        // if we don't have any characters from an atom (including prefix) then we may have just completed parsing a list
        if(atomStringBuilder.isEmpty() && prefixBuilder.isEmpty()) {
            return;
        }

        // if we're within a string literal (seen open-quote but not close-quote) then treat space as just another character for the current atom
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
        atomStringBuilder.delete(0, atomStringBuilder.length());

        prefixBuilder.delete(0, prefixBuilder.length());
        suffixBuilder.delete(0, suffixBuilder.length());

        syntaxTreeBuilder.newAtom(atomBuilder);
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

    public void startList(CharacterReaderEvent event) {
        // Determine the entire prefix - may be null, 1 or 2 characters based on current knowledge
        String prefix = prefixBuilder.length() != 0 ? prefixBuilder.toString() : null;

        syntaxTreeBuilder.startList(event.depth()-1, prefix);

        prefixBuilder.delete(0, prefixBuilder.length());  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read
    }

    public void reset() {
        prefixBuilder.delete(0, prefixBuilder.length());
        suffixBuilder.delete(0, suffixBuilder.length());
        atomStringBuilder.delete(0, atomStringBuilder.length());
        syntaxTreeBuilder.reset();
    }
}
