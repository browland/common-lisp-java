package syntaxtree;

import java.util.Optional;

public enum QuoteType {
    QUOTE, FUNCTION_QUOTE, QUASIQUOTE, UNQUOTE, UNQUOTE_SPLICING;

    /**
     * Returns Optional.empty() when seeing an incompleted prefix sequence (currently
     * the only case is where we see the hash character this time, and the next invocation
     * will be the single quote).  This disconnect happens because upstream we identify
     * individual characters which should be treated as part of a quote sequence, but here
     * we can only map the entire sequence to a QuoteType.
     * Unfortunately this means currently we don't really handle unknown quote prefixes.
     * This would indicate a programming error (upstream we identified a quote character
     * sequence, but we didn't handle it here).
     */
    public static Optional<QuoteType> ofPrefix(String quotePrefix) {
        return switch (quotePrefix) {
            case QuotePrefix.QUOTE -> Optional.of(QuoteType.QUOTE);
            case QuotePrefix.FUNCTION_QUOTE -> Optional.of(QuoteType.FUNCTION_QUOTE);
            case QuotePrefix.QUASIQUOTE -> Optional.of(QuoteType.QUASIQUOTE);
            case QuotePrefix.UNQUOTE -> Optional.of(QuoteType.UNQUOTE);
            case QuotePrefix.UNQUOTE_SPLICING -> Optional.of(QuoteType.UNQUOTE_SPLICING);
            default -> Optional.empty();
        };
    }
}
