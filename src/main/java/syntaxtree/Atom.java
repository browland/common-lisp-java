package syntaxtree;

public record Atom(String value, String prefix, String suffix) implements Node {

    final static class Builder implements NodeBuilder {
        private String prefix;
        private String suffix;
        private String value;

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Atom build() {
            return new Atom(this.value, this.prefix, this.suffix);
        }
    }

    public QuoteType quoteType() {
        if("\"".equals(prefix) && "\"".equals(suffix)) {
            return QuoteType.STRING;
        }
        else if(":".equals(prefix)) {
            return QuoteType.KEYWORD;
        }
        else {
            return null;
        }
    }
}
