package parser;

import java.util.List;

public record Node(NodeType type,
                   String rawText,
                   String formRawText,
                   List<Node> children) {
}
