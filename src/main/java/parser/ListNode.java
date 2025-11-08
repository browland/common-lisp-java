package parser;

public class ListNode {
    private String value;          // either it's an atom ...
    private ParsedList parsedList; // or a parsed list of nodes
    private QuoteType quoteType;        // supporting the case where value is a quoted function e.g. #'even.  Makes no sense for a parsedList.

    public ListNode(String value) {
        this.value = value;
        this.quoteType = QuoteType.NONE;
    }

    public ListNode(String value, QuoteType quoteType) {
        this.value = value;
        this.quoteType = quoteType;
    }

    public ListNode(ParsedList parsedList) {
        this.parsedList = parsedList;
    }

    public String toString() {
        return value != null ? value : parsedList.toString();
    }

    public boolean equals(Object o) {
        if(!(o instanceof ListNode)) {
            return false;
        }

        ListNode other = (ListNode)o;
        if(this.value != null && other.value != null && this.value.equals(other.value)) {
            return true;
        }

        if(this.parsedList != null && other.parsedList != null && this.parsedList.equals(other.parsedList)) {
            return true;
        }

        return false;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public ParsedList getParsedList() {
        return parsedList;
    }

    public QuoteType getQuoteType() {
        return quoteType;
    }
}
