package syntaxtree;

public class SyntaxTreeBuilder {
    private RList.Builder currentListBuilder;  // This is updated to point to the list we're currently building
    private boolean finished;
    private int depth = -1;  // we start from -1 as the first open-bracket we see will increment it to 0, which is the correct
                             // depth of the top-level list.
    private boolean insertingQuote;

    void newAtom(Atom.Builder atomBuilder) {
        currentListBuilder.addNodeBuilder(atomBuilder);  // todo need to collect the prefix into whatever we add here, not just a string

        if(insertingQuote) {
            this.endList();
            this.insertingQuote = false;
        }
    }

    void startList(String prefix) {
        depth++;

        RList.Builder tempListBuilder = new RList.Builder()
                .parentListBuilder(currentListBuilder)
                .depth(depth)
                .prefix(prefix)
                .quoted(insertingQuote);
        this.insertingQuote = false;

        if(currentListBuilder != null) {
            currentListBuilder.addNodeBuilder(tempListBuilder);
        }

        currentListBuilder = tempListBuilder;
    }

    void endList() {
        depth--;
        // check whether the list which has just finished is the top-level list
        if(currentListBuilder.getDepth() == 0) {
            finished = true;
        }

        boolean isQuoted = currentListBuilder.isQuoted();

        currentListBuilder = currentListBuilder.getParentListBuilder() != null ?
                currentListBuilder.getParentListBuilder() : currentListBuilder;

        if(isQuoted) {
            endList();
        }
    }

    void insertQuote(QuoteType quoteType) {
        this.startList(null);

        String quoteText = switch(quoteType) {
            case QUOTE -> "quote";
            case FUNCTION_QUOTE -> "function";
            case QUASIQUOTE -> "quasiquote";
            case UNQUOTE -> "unquote";
        };

        Atom.Builder quoteAtomBuilder = new Atom.Builder()
                .value(quoteText);

        this.newAtom(quoteAtomBuilder);

        this.insertingQuote = true;
    }

    public void reset() {
        currentListBuilder = null;
        finished = false;
    }

    public boolean isFinished() {
        return finished;
    }

    public RList getResult() {
        if(depth != -1) {
            throw new IllegalStateException("Can't get result while not at top level");
        }
        return currentListBuilder.build();
    }
}
