package syntaxtree;

public sealed interface NodeBuilder permits Atom.Builder, RList.Builder {
    Node build();

}
