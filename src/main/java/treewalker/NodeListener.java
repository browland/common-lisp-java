package treewalker;

import java.util.List;

public interface NodeListener {
    void handleAtom(TypedAtom<?> atom);
    void startForm();
    ProcessedForm processForm(List<ProcessedNode> processedNodes);
}
