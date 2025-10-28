package parser;

import java.util.List;

/**
 * Represents a text node in the tree parsed from a form.
 */
public record Node(NodeType type,
                   String rawText,
                   String parentFormRawText,
                   List<Node> children) {
}
