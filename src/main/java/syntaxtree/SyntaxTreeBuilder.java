package syntaxtree;

public class SyntaxTreeBuilder {
    private RList.Builder currentListBuilder;  // This is updated to point to the list we're currently building
    private boolean finished;

    void newAtom(Atom.Builder atomBuilder) {
        currentListBuilder.addNodeBuilder(atomBuilder);  // todo need to collect the prefix into whatever we add here, not just a string
    }

    void startList(int depth, String prefix) {
        RList.Builder tempListBuilder = new RList.Builder()
                .parentListBuilder(currentListBuilder)
                .depth(depth)
                .prefix(prefix);

        if(currentListBuilder != null) {
            currentListBuilder.addNodeBuilder(tempListBuilder);
        }

        currentListBuilder = tempListBuilder;
    }

    void endList() {
        // check whether the list which has just finished is the top-level list
        if(currentListBuilder.getDepth() == 0) {
            finished = true;
        }

        currentListBuilder = currentListBuilder.getParentListBuilder() != null ?
                currentListBuilder.getParentListBuilder() : currentListBuilder;
    }

    void reset() {
        currentListBuilder = null;
        finished = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public RList getResult() {
        return currentListBuilder.build();
    }
}
