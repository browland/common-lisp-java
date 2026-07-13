package treewalker;

import java.util.List;

public interface NodeListener {
    void handleAtom(TypedAtom<?> atom, int pos);
    void startForm();
    ProcessedForm processForm(List<ProcessedNode> processedNodes);
    String generate();
}
