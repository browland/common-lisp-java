package parser;

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class ListParser {

    public ParsedList parse(String program) throws IOException {
        // PushbackReader so we can 'reset' characters back into the stream e.g. when we read 'too far' by encountering
        // an opening bracket which 'belongs' to a recursive parsing call.
        return parse(new PushbackReader(new StringReader(program)), QuoteType.NONE);
    }

    public ParsedList parse(PushbackReader reader, QuoteType quoteType) throws IOException {
        boolean inList = false;           // indicates we've seen the opening bracket for the current list.
                                          // Once true, any other opening bracket means a sub-list so we should recurse.
        boolean detectedQuotedList = false;       // indicates that we've detected a quoted list - the next loop iteration
                                          // needs this to pass to the recursive parse() call.
        boolean detectedQuotedFunction = false;   // indicates that we've detected a quoted function - the next loop iteration
                                          // needs this to pass to the recursive parse() call.
        boolean detectedOtherQuoteType = false;

        List<ListNode> nodes = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        int i = reader.read();
        while (i != -1) {
            char c = (char)i;

            // consume open bracket for this list
            if(!inList && c == '(') {
                inList = true;
            }

            // space or close list means this node is completely read
            else if((c == ' ' || c == ')')) {
                if(!sb.isEmpty()) {
                    // determine quote type
                    // todo bailing out to 'other'
                    QuoteType listNodeQuoteType = detectedQuotedFunction ? QuoteType.FUNCTION : (detectedOtherQuoteType ? QuoteType.OTHER : QuoteType.NONE);

                    nodes.add(new ListNode(sb.toString(), listNodeQuoteType));
                    if(detectedQuotedFunction) {
                        detectedQuotedFunction = false;
                    }
                }
                if(c == ')') {
                    return new ParsedList(nodes, quoteType);
                }
                // otherwise get ready for the next part of this list
                sb = new StringBuilder();
            }

            // opening bracket means new list if we're already in the current list
            else if (c == '(' && inList) {
                // we need to 'un-read' the opening bracket so the recursive parse sees it
                reader.unread(c);

                // we need to recurse; resolve the quotation type if any
                QuoteType childListQuotationType = detectedQuotedList ? QuoteType.LIST : (detectedQuotedFunction ? QuoteType.FUNCTION : QuoteType.NONE);
                ParsedList subListForThisNode = parse(reader, childListQuotationType);
                detectedQuotedList = false;
                detectedQuotedFunction = false;
                nodes.add(new ListNode(subListForThisNode));
            }

            else if(c == '\'') {
                // check whether this is a quoted list coming up otherwise crash
                char nextChar = (char)reader.read();
                if(nextChar == '(') {
                    detectedQuotedList = true;
                }
                else {
                    detectedOtherQuoteType = true;
                }
                reader.unread(nextChar);
            }

            else if(c == '#') {
                // for now we only deal with quoted functions otherwise crash
                char nextChar = (char)reader.read();
                if(nextChar != '\'') {
                    throw new IllegalStateException("hash with unsupported next character");
                }
                detectedQuotedFunction = true;
            }

            else if(c == '"') {
                // if it's a string we read the entire string right here, to avoid special handling on subsequent loop iterations
                char nextChar = (char)reader.read();
                while(nextChar != '"') {
                    sb.append(nextChar);
                    nextChar = (char)reader.read();
                }

                // consume trailing space if it's there
                nextChar = (char)reader.read();
                if(nextChar != ' ') {
                    reader.unread(nextChar);
                }

                nodes.add(new ListNode(sb.toString(), QuoteType.STRING));
            }

            else {
                // if any other char then it's part of the current atom
                sb.append(c);
            }

            i = reader.read();
        }

        return new ParsedList(nodes, QuoteType.NONE);
    }
}
