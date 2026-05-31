package reader;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class NewReader {
    private final StringBuilder stringBuilder = new StringBuilder();
    private ParseNode currentNode;

    public List<ParseElement> parse(String program) {
        int pos = 0;
        this.currentNode = start;

        List<ParseElement> parseElements = new ArrayList<>();

        while(pos < program.length()) {
            Optional<ParseElement> optionalNextElement = nextParseElement(program, pos);
            if(optionalNextElement.isPresent()) {
                ParseElement parseElement = optionalNextElement.get();
                parseElements.add(parseElement);
                pos = pos + parseElement.matchedChars.length();
            }
        }

        return parseElements;
    }

    public Optional<ParseElement> nextParseElement(String program, int pos) {
        if(pos > program.length()-1) {
            return Optional.empty();
        }

        char currentChar = program.charAt(pos);
        List<ParseNode> nextNodes = currentNode.getNextNodes(currentChar);
        List<ParseNode> nextNodesPreferringTransitions = sortPreferringIncreasingDepth(currentNode, nextNodes);
        System.out.println("current char %s next nodes %s".formatted(currentChar, nextNodesPreferringTransitions));
        for (ParseNode matchedNode : nextNodesPreferringTransitions) {
            // Avoid reading off the end of the string on next recursion by checking if this matched node has EndNode attached; if so we're done
            if(pos == program.length()-1) {
                Optional<EndNode> optionalAttachedEndNode = matchedNode.attachedEndNode();
                if(optionalAttachedEndNode.isPresent()) {
                    stringBuilder.append(currentChar);
                    EndNode endNode = optionalAttachedEndNode.get();
                    Optional<ParseElement> result = Optional.of(new ParseElement(endNode.parseElementType, stringBuilder.toString()));
                    reset();
                    return result;
                }
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

    private List<ParseNode> sortPreferringIncreasingDepth(ParseNode currentNode, List<ParseNode> nextNodes) {
        ParseNode savedEndNode = null;
        ParseNode savedSelfNode = null;

        List<ParseNode>  result = new ArrayList<>();
        for(ParseNode parseNode : nextNodes) {
            if(parseNode == currentNode) {
                savedSelfNode = parseNode;
            }
            else if(parseNode instanceof EndNode) {
                savedEndNode = parseNode;
            }
            else {
                result.add(parseNode);
            }
        }

        if(savedSelfNode != null) {
            result.add(savedSelfNode);
        }
        if(savedEndNode != null) {
            result.add(savedEndNode);
        }
        return result;
    }

    private void reset() {
        currentNode = start;
        stringBuilder.delete(0, stringBuilder.length());
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

        public String toString() {
            return "MatchNode: matchChar: %s, stackAction: %s".formatted(matchChar, stackAction);
        }
    }

    public record ParseElement(ParseElementType parseElementType,
                               String matchedChars) {

    }

    enum ParseElementType {
        BLOCK_COMMENT, BARE_WHITESPACE
    }

    // Block comment nodes
    private StartNode start = new StartNode();
        private MatchNode openHash = new MatchNode("#", null);
        private MatchNode openPipe = new MatchNode("|", StackAction.PUSH);
        private MatchNode commentChar = new MatchNode("(*)", null);
        private MatchNode closePipe = new MatchNode("|", null);
        private MatchNode closeHash = new MatchNode("#", StackAction.POP);
        private ParseNode endBlockComment = new EndNode(ParseElementType.BLOCK_COMMENT);

        private MatchNode bareWhitespace = new MatchNode(" ", null);
        private ParseNode endBareWhitespace = new EndNode(ParseElementType.BARE_WHITESPACE);

    // Block comment links
    {
        // All transitions from start
        start.setNextNodes(Set.of(openHash, bareWhitespace));

        // Block comment
        openHash.setNextNodes(Set.of(openPipe));
        openPipe.setNextNodes(Set.of(commentChar));
        commentChar.setNextNodes(Set.of(openHash, commentChar, closePipe));
        closePipe.setNextNodes(Set.of(closeHash));
        closeHash.setNextNodes(Set.of(commentChar, endBlockComment));

        // Bare whitespace
        bareWhitespace.setNextNodes(Set.of(bareWhitespace, endBareWhitespace));
    }

}
