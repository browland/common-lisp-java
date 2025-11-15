package reader;

public class Reader {
    private final StringBuilder prefixBuilder = new StringBuilder();
    private final StringBuilder atomStringBuilder = new StringBuilder();

    private RList.Builder listBuilder;
    private String prefix;

    void inAtom(CharacterReaderEvent event) {
        atomStringBuilder.append(event.character());
    }

    void inPrefix(CharacterReaderEvent event) {
        prefixBuilder.append(event.character());
        prefix = prefixBuilder.toString();
    }

    void endNode(CharacterReaderEvent event) {
        // if we don't have any characters from an atom then we may have just completed parsing a list
        if(atomStringBuilder.isEmpty()) {
            return;
        }

        String atom = atomStringBuilder.toString();
        Atom.Builder atomBuilder = new Atom.Builder()
                .value(atom)
                .prefix(prefix);
        atomStringBuilder.delete(0, atomStringBuilder.length());

        prefixBuilder.delete(0, atomStringBuilder.length());

        listBuilder.addNodeBuilder(atomBuilder);  // todo need to collect the prefix into whatever we add here, not just a string
    }

    void endList(CharacterReaderEvent event) {
        if(!atomStringBuilder.isEmpty()) {
            Atom.Builder atomBuilder = new Atom.Builder()
                    .value(atomStringBuilder.toString())
                    .prefix(prefix);
            listBuilder.addNodeBuilder(atomBuilder);
            atomStringBuilder.delete(0, atomStringBuilder.length());
        }

        listBuilder = listBuilder.getParentListBuilder() != null ? listBuilder.getParentListBuilder() : listBuilder;
    }

    void startList(CharacterReaderEvent event) {
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
}
