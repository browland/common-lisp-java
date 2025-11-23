package reader;

public record Atom(String value, String prefix) implements Node {

    final static class Builder implements NodeBuilder {
        private String prefix;
        private String value;

        public Builder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Atom build() {
            return new Atom(this.value, this.prefix);
        }
    }

}
