package parser;

import java.util.List;

public class ParsedList {
    private List<ListNode> nodes;
    private boolean quoted;

    public ParsedList(List<ListNode> nodes, boolean quoted) {
        this.nodes = nodes;
        this.quoted = quoted;
    }

    public List<ListNode> getNodes() {
        return nodes;
    }

    public boolean isQuoted() {
        return quoted;
    }

    public String toString() {
        return nodes.toString();
    }
}
