package reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimplerTokeniser {
    private State state;
    private int blockCommentDepth = 0;

    /**
     * Returns list of tokens in this program, e.g. values, atoms, symbols.
     * In particular though, for lists, we return open bracket or close bracket separately from the atomic values which
     * may be within.  So we leave it to the next layer up to build lists or forms etc from these tokens.
     */
    public List<String> tokenise(String program) {
        int pos = 0;
        List<String> tokens = new ArrayList<>();
        while(pos < program.length()) {
            // peek next char as it helps narrow down the next datum
            char firstChar = program.charAt(pos);

            if(state == State.IN_BLOCK_COMMENT) {
                pos++;
            }
            else if(Character.isDigit(firstChar)) {
                TokenResult tokenResult = handleDigits(program, pos);
                tokens.add(tokenResult.token);
                pos+=tokenResult.charsConsumed;
            }
            else if(firstChar == '(') {
                tokens.add("(");
                pos++;
            }
            else if(firstChar == ')') {
                tokens.add(")");
                pos++;
            }
            else if(firstChar == '\'') {
                tokens.add("'");
                pos++;
            }
            else if(firstChar == '`') {
                tokens.add("`");
                pos++;
            }
            else if(firstChar == '"') {
                Optional<TokenResult> optionalTokenResult = handleString(program, pos);
                if(optionalTokenResult.isPresent()) {
                    TokenResult tokenResult = optionalTokenResult.get();
                    tokens.add(tokenResult.token);
                    pos+=tokenResult.charsConsumed;
                }
                else {
                    throw new RuntimeException("not seen end of string in " + program);
                }
            }
            else if(firstChar == '#') {
                // only an optional token here as it may be a block comment
                Optional<TokenResult> optionalToken = handleHash(program, pos);
                if(optionalToken.isPresent()) {
                    TokenResult tokenResult = optionalToken.get();
                    if(tokenResult.emit) {
                        String token = tokenResult.token;
                        tokens.add(token);
                    }
                    pos+=tokenResult.charsConsumed();
                }
                else {
                    // We didn't complete the token; so far this happens for a block comment and we may have only
                    // received part of it so far.  We retain state so on the next call we can pick up from where we left off.
                }
            }
            else if(program.length() > 2 && program.substring(pos, pos+2).equals("|#")) {
                handleCloseBlockComment();
                pos+=2;
            }
            else if(Character.isWhitespace(firstChar)) {
                int charsConsumed = consumeWhitespace(program, pos);
                pos+=charsConsumed;
                state = null;  // Clear any
            }
            else {
                // only possibility left is a simple atomic value - read up to the first whitespace char (not including it)
                Optional<TokenResult> optionalToken = handleAtom(program, pos);
                if(optionalToken.isPresent()) {
                    TokenResult tokenResult = optionalToken.get();
                    String token = tokenResult.token;
                    tokens.add(token);
                    pos += tokenResult.charsConsumed();
                }
            }
        }
        return tokens;
    }

    private Optional<TokenResult> handleAtom(String program, int pos) {
        StringBuilder stringBuilder = new StringBuilder();
        char c = program.charAt(pos);

        while(!(Character.isWhitespace(c) || c == ')')) {
            stringBuilder.append(c);

            if(pos == program.length()-1) {
                break;
            }
            pos++;
            c = program.charAt(pos);
        }

        String atomString = stringBuilder.toString();
        return Optional.of(new TokenResult(atomString, atomString.length(), atomString.length(), true));
    }

    private Optional<TokenResult> handleString(String program, int pos) {
        boolean escaping = false;
        StringBuilder stringBuilder = new StringBuilder();
        // Have already checked first char above
        stringBuilder.append('"');
        pos++;
        while(pos < program.length()) {
            char c = program.charAt(pos);
            if(c == '"') {
                if(escaping) {
                    escaping = false;
                    stringBuilder.append(c);
                    pos++;
                }
                else {
                    stringBuilder.append(c);
                    return Optional.of(new TokenResult(stringBuilder.toString(), stringBuilder.length(), stringBuilder.length(), true));
                }
            }
            else {
                if(c == '\\') {
                    escaping = true;
                }
                stringBuilder.append(c);
                pos++;
            }
        }
        // problem as we've not seen end of string
        return Optional.empty();
    }

    private TokenResult handleDigits(String program, int pos) {
        StringBuilder stringBuilder = new StringBuilder();
        char c = program.charAt(pos);
        while(Character.isDigit(c) || c == '.' || c == '/' || c == 's' || c == 'd') {
            stringBuilder.append(c);
            pos++;
            if(pos < program.length()) {
                c = program.charAt(pos);
            }
            else {
                break;
            }
        }
        return new TokenResult(stringBuilder.toString(), stringBuilder.length(), stringBuilder.length(), true);
    }

    private void handleCloseBlockComment() {
        blockCommentDepth--;
        if(blockCommentDepth == 0) {
            state = null;
        }
    }

    private int consumeWhitespace(String program, int pos) {
        int charsConsumed = 0;
        char c = program.charAt(pos);
        while(Character.isWhitespace(c)) {
            charsConsumed++;
            pos++;
            c = program.charAt(pos);
        }
        return charsConsumed;
    }

    private Optional<TokenResult> handleHash(String program, int pos) {
        // Character literal
        if(program.substring(pos, pos+2).equals("#\\")) {
            return handleAtom(program, pos);
        }
        else if(program.substring(pos, pos+2).equals("#|")) {
            return openBlockComment(program, pos);
        }
        else if(program.substring(pos, pos+2).equals("#C")) {
            return readLiteralTerminatedByCloseBracket(program, pos);
        }
        else if(program.substring(pos, pos+2).equals("#(")) {
            return readLiteralTerminatedByCloseBracket(program, pos);
        }
        else {
            // treat it like a regular atom e.g. #b0101
            return handleAtom(program, pos);
        }
    }

    private Optional<TokenResult> readLiteralTerminatedByCloseBracket(String program, int pos) {
        // read up to (and including) the close bracket
        StringBuilder complexLiteral = new StringBuilder();
        for(char c : program.substring(pos).toCharArray()) {
            complexLiteral.append(c);
            if(c == ')') {
                break;
            }
        }
        String complexLiteralString = complexLiteral.toString();
        return Optional.of(new TokenResult(complexLiteralString, complexLiteralString.length(), complexLiteralString.length(), true));
    }

    private Optional<TokenResult> openBlockComment(String program, int pos) {
        state = State.IN_BLOCK_COMMENT;
        blockCommentDepth++;
        return Optional.of(new TokenResult(null, 2, 2, false));
    }

    record TokenResult(String token, int charsConsumed, int charsConsumedThisTime, boolean emit) {}

    enum State {
        IN_BLOCK_COMMENT
    }
}
