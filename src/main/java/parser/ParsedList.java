package parser;

import java.util.List;

public class ParsedList {
    private List<ListNode> nodes;
    private QuoteType quoteType;

    public ParsedList(List<ListNode> nodes, QuoteType quoteType) {
        this.nodes = nodes;
        this.quoteType = quoteType;
    }

    public List<ListNode> getNodes() {
        return nodes;
    }

    public QuoteType getQuoteType() {
        return quoteType;
    }

    public String toString() {
        return nodes.toString();
    }
}
