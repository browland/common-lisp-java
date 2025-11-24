package syntaxtree;

import reader.CharacterReaderEvent;

public class SyntaxTreeBuilder {
    private final StringBuilder prefixBuilder = new StringBuilder();
    private final StringBuilder suffixBuilder = new StringBuilder();
    private final StringBuilder atomStringBuilder = new StringBuilder();

    private RList.Builder listBuilder;
    private String prefix;
    private String suffix;

    public void inAtom(CharacterReaderEvent event) {
        if(!suffixBuilder.isEmpty()) {
            throw new IllegalArgumentException("Seeing atom characters after suffix quote");
        }
        atomStringBuilder.append(event.character());
    }

    public void onQuoteChar(CharacterReaderEvent event) {
        if(atomStringBuilder.isEmpty()) {
            prefixBuilder.append(event.character());
            prefix = prefixBuilder.toString();
        }
        else if(!atomStringBuilder.isEmpty()) {
            suffixBuilder.append(event.character());
            suffix = suffixBuilder.toString();
        }
    }

    public void onSpace(CharacterReaderEvent event) {
        // if we don't have any characters from an atom (including prefix) then we may have just completed parsing a list
        if(atomStringBuilder.isEmpty() && prefixBuilder.isEmpty()) {
            return;
        }

        // if we're in a string literal then treat space as just another character for the current atom
        if(!prefixBuilder.isEmpty() && "\"".equals(prefix)) {
            atomStringBuilder.append(event.character());
            return;
        }

        // otherwise this space signifies the end of the atom we've been building
        String atom = atomStringBuilder.toString();
        Atom.Builder atomBuilder = new Atom.Builder()
                .value(atom)
                .prefix(prefix)
                .suffix(suffix);
        atomStringBuilder.delete(0, atomStringBuilder.length());

        prefixBuilder.delete(0, prefixBuilder.length());
        suffixBuilder.delete(0, suffixBuilder.length());

        listBuilder.addNodeBuilder(atomBuilder);  // todo need to collect the prefix into whatever we add here, not just a string
    }

    public void endList(CharacterReaderEvent event) {
        if(!atomStringBuilder.isEmpty()) {
            Atom.Builder atomBuilder = new Atom.Builder()
                    .value(atomStringBuilder.toString())
                    .prefix(prefix)
                    .suffix(suffix);
            listBuilder.addNodeBuilder(atomBuilder);
            atomStringBuilder.delete(0, atomStringBuilder.length());
        }

        listBuilder = listBuilder.getParentListBuilder() != null ? listBuilder.getParentListBuilder() : listBuilder;
    }

    public void startList(CharacterReaderEvent event) {
        RList.Builder tempListBuilder = new RList.Builder()
                .parentListBuilder(listBuilder)
                .depth(event.depth()-1)
                .prefix(prefix);

        prefixBuilder.delete(0, prefixBuilder.length());  // whenever we consume 'prefix' we need to reset the flag to say prefix has been read

        if(listBuilder != null) {
            listBuilder.addNodeBuilder(tempListBuilder);
        }
        listBuilder = tempListBuilder;
    }

    public RList getResult() {
        return listBuilder.build();
    }

    public void reset() {
        prefixBuilder.delete(0, prefixBuilder.length());
        suffixBuilder.delete(0, suffixBuilder.length());
        atomStringBuilder.delete(0, atomStringBuilder.length());
        listBuilder = null;
        prefix = null;
    }
}
