package reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimplerTokeniser {
    private boolean withinBlockComment = false;
    private int blockCommentDepth = 0;

    /**
     * Returns list of tokens in this program, e.g. values, atoms, symbols.
     * In particular though, for lists, we return open bracket or close bracket separately from the atomic values which
     * may be within.  So we leave it to the next layer up to build lists or forms etc from these tokens.
     */
    public List<String> tokenise(String program) {
        int pos = 0;
        List<String> tokens = new ArrayList<>();
        while(pos < program.length()-1) {
            // peek next char as it helps narrow down the next datum
            char firstChar = program.charAt(pos);

            if(withinBlockComment) {
                pos++;
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
            else if(program.substring(pos, pos+2).equals("|#")) {
                handleCloseBlockComment();
                pos+=2;
            }
            else if(Character.isWhitespace(firstChar)) {
                int charsConsumed = consumeWhitespace(program, pos);
                pos+=charsConsumed;
            }
            else {
                throw new UnsupportedOperationException("not yet handled");
            }
        }
        return tokens;
    }

    private void handleCloseBlockComment() {
        blockCommentDepth--;
        if(blockCommentDepth == 0) {
            withinBlockComment = false;
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
            return readCharacterLiteral(program, pos);
        }
        else if(program.substring(pos, pos+2).equals("#|")) {
            return openBlockComment(program, pos);
        }
        else {
            return Optional.empty();
        }
    }

    private Optional<TokenResult> openBlockComment(String program, int pos) {
        withinBlockComment = true;
        blockCommentDepth++;
        return Optional.of(new TokenResult(null, 2, 2, false));
    }

    private static Optional<TokenResult> readCharacterLiteral(String remainingProgram, int pos) {
        // read up to next whitespace or close bracket to get full character literal.  If the char literal is the last
        // thing on the line that'll still work as the loop will correctly terminate at end of string.
        StringBuilder charLiteral = new StringBuilder();
        for(char c : remainingProgram.substring(pos).toCharArray()) {
            if(! (Character.isWhitespace(c) || c == ')')) {
                charLiteral.append(c);
            }
            else {
                break;
            }
        }
        String charLiteralString = charLiteral.toString();
        return Optional.of(new TokenResult(charLiteralString, charLiteralString.length(), charLiteralString.length(), true));
    }

    record TokenResult(String token, int charsConsumed, int charsConsumedThisTime, boolean emit) {}
}
