package syntaxtree;

import reader.CharacterReaderEvent;

/**
 * This is more complex than the layer below (CharacterReader).
 * This receives character-level events and builds complete atoms and lists incrementally as events are received.
 * For this reason, it maintains a fair bit of state.
 */
public class SyntaxTreeBuilder {
    private final StringBuilder prefixBuilder = new StringBuilder();
    private final StringBuilder suffixBuilder = new StringBuilder();
    private final StringBuilder atomStringBuilder = new StringBuilder();

    private RList.Builder currentListBuilder;  // This is updated to point to the list we're currently building
    private boolean finished;

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

        currentListBuilder.addNodeBuilder(atomBuilder);  // todo need to collect the prefix into whatever we add here, not just a string
    }

    public void endList(CharacterReaderEvent event) {
        if(!atomStringBuilder.isEmpty()) {
            String prefix = prefixBuilder.length() != 0 ? prefixBuilder.toString() : null;
            String suffix = suffixBuilder.length() != 0 ? suffixBuilder.toString() : null;
            Atom.Builder atomBuilder = new Atom.Builder()
                    .value(atomStringBuilder.toString())
                    .prefix(prefix)
                    .suffix(suffix);
            currentListBuilder.addNodeBuilder(atomBuilder);
            atomStringBuilder.delete(0, atomStringBuilder.length());
        }

        // check whether the list which has just finished is the top-level list
        if(currentListBuilder.getDepth() == 0) {
            finished = true;
        }

        currentListBuilder = currentListBuilder.getParentListBuilder() != null ? currentListBuilder.getParentListBuilder() : currentListBuilder;
    }

    public void startList(CharacterReaderEvent event) {
        String prefix = prefixBuilder.length() != 0 ? prefixBuilder.toString() : null;
        RList.Builder tempListBuilder = new RList.Builder()
                .parentListBuilder(currentListBuilder)
                .depth(event.depth()-1)
                .prefix(prefix);

        prefixBuilder.delete(0, prefixBuilder.length());  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read

        if(currentListBuilder != null) {
            currentListBuilder.addNodeBuilder(tempListBuilder);
        }
        currentListBuilder = tempListBuilder;
    }

    public RList getResult() {
        return currentListBuilder.build();
    }

    public void reset() {
        prefixBuilder.delete(0, prefixBuilder.length());
        suffixBuilder.delete(0, suffixBuilder.length());
        atomStringBuilder.delete(0, atomStringBuilder.length());
        currentListBuilder = null;
        finished = false;
    }

    public boolean isFinished() {
        return finished;
    }
}
