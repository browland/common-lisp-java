package parser;

import java.util.List;

public record Node(NodeType type,
                   String text,
                   List<Node> children) {
}
