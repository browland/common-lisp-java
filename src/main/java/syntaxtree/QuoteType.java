package syntaxtree;

import java.util.Optional;

public enum QuoteType {
    QUOTE, FUNCTION_QUOTE, QUASIQUOTE, UNQUOTE;

    public static Optional<QuoteType> ofPrefix(String quotePrefix) {
        return switch (quotePrefix) {
            case QuotePrefix.QUOTE -> Optional.of(QuoteType.QUOTE);
            case QuotePrefix.FUNCTION_QUOTE -> Optional.of(QuoteType.FUNCTION_QUOTE);
            case QuotePrefix.QUASIQUOTE -> Optional.of(QuoteType.QUASIQUOTE);
            case QuotePrefix.UNQUOTE -> Optional.of(QuoteType.UNQUOTE);
            default -> Optional.empty();
        };
    }
}
