package reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NewReader {
    private final StringBuilder stringBuilder = new StringBuilder();
    private ParseNode currentNode;

    public Optional<ParseElement> nextParseElement(String program) {
        this.currentNode = start;
        Optional<ParseElement> parseElement = nextParseElement(program, 0);
        return parseElement;
    }

    public Optional<ParseElement> nextParseElement(String program, int pos) {
        if(pos > program.length()-1) {
            return Optional.empty();
        }

        char currentChar = program.charAt(pos);
        List<ParseNode> nextNodes = currentNode.getNextNodes(currentChar);
        for (ParseNode matchedNode : nextNodes) {
            // check if this matched node has EndNode attached; if so we're done
            Optional<EndNode> optionalAttachedEndNode = matchedNode.attachedEndNode();
            if(optionalAttachedEndNode.isPresent()) {
                stringBuilder.append(currentChar);
                EndNode endNode = optionalAttachedEndNode.get();
                return Optional.of(new ParseElement(endNode.parseElementType, stringBuilder.toString()));
            }

            currentNode = matchedNode;
            stringBuilder.append(currentChar);
            Optional<ParseElement> parseElement = nextParseElement(program, pos + 1);
            if(parseElement.isPresent()) {
                return parseElement;
            }
            stringBuilder.deleteCharAt(pos);
        }
        return Optional.empty();
    }

    enum StackAction {
        PUSH, POP
    }

    interface ParseNode {
        List<ParseNode> getNextNodes(char c);
        boolean matches(char c);
        Optional<EndNode> attachedEndNode();
    }

    static class StartNode implements ParseNode {
        private Set<ParseNode> nextNodes;

        public void setNextNodes(Set<ParseNode> nextNodes) {
            this.nextNodes = nextNodes;
        }

        public List<ParseNode> getNextNodes(char c) {
            List<ParseNode> matchedNodes = new ArrayList<>();
            for (ParseNode child : nextNodes) {
                if (child.matches(c)) {
                    matchedNodes.add(child);
                }
            }
            return matchedNodes;
        }

        public boolean matches(char c) {
            return true;
        }

        public Optional<EndNode> attachedEndNode() {
            return Optional.empty();
        }
    }

    static class EndNode implements ParseNode {
        private ParseElementType parseElementType;
        public EndNode(ParseElementType parseElementType) {
            this.parseElementType = parseElementType;
        }

        public boolean matches(char c) {
            return true;
        }

        public List<ParseNode> getNextNodes(char c) {
            return List.of();
        }

        public Optional<EndNode> attachedEndNode() {
            throw new IllegalStateException("already at end node");
        }
    }

    static class MatchNode implements ParseNode {
        private String matchChar;
        private StackAction stackAction;
        private Set<ParseNode> nextNodes;

        MatchNode(String matchChar, StackAction stackAction) {
            this.matchChar = matchChar;
            this.stackAction = stackAction;
        }

        public void setNextNodes(Set<ParseNode> nextNodes) {
            this.nextNodes = nextNodes;
        }

        public boolean matches(char c) {
            if ("(*)".equals(matchChar)) {
                return true;
            } else {
                if (matchChar.length() != 1) {
                    return false;
                } else return matchChar.charAt(0) == c;
            }
        }

        public List<ParseNode> getNextNodes(char c) {
            List<ParseNode> matchedNodes = new ArrayList<>();
            for (ParseNode child : nextNodes) {
                if (child.matches(c)) {
                    matchedNodes.add(child);
                }
            }
            return matchedNodes;
        }

        public Optional<EndNode> attachedEndNode() {
            for(ParseNode parseNode : nextNodes) {
                if(parseNode instanceof EndNode) {
                    return Optional.of((EndNode)parseNode);
                }
            }
            return Optional.empty();
        }
    }

    public record ParseElement(ParseElementType parseElementType,
                               String matchedChars) {

    }

    enum ParseElementType {
        BLOCK_COMMENT,
    }

    // Block comment nodes
    private StartNode start = new StartNode();
    private MatchNode openHash = new MatchNode("#", null);
    private MatchNode openPipe = new MatchNode("|", StackAction.PUSH);
    private MatchNode commentChar = new MatchNode("(*)", null);
    private MatchNode closePipe = new MatchNode("|", null);
    private MatchNode closeHash = new MatchNode("#", StackAction.POP);
    private ParseNode end = new EndNode(ParseElementType.BLOCK_COMMENT);

    // Block comment links
    {
        start.setNextNodes(Set.of(openHash));
        openHash.setNextNodes(Set.of(openPipe));
        openPipe.setNextNodes(Set.of(commentChar));
        commentChar.setNextNodes(Set.of(openHash, commentChar, closePipe));
        closePipe.setNextNodes(Set.of(closeHash));
        closeHash.setNextNodes(Set.of(commentChar, end));
    }

}
