package compiler;

import compiler.treewalker.TreeWalker;
import reader.NodeBuilder;
import syntaxtree.Node;

import java.util.List;

public class Compiler {
    private final TreeWalker walker;
    public Compiler(boolean loadScripts) {
        walker = new TreeWalker(loadScripts);
    }

    public void compile(String program) {
        NodeBuilder nodeBuilder = new NodeBuilder();
        List<Node> nodes = nodeBuilder.build(program);
        walker.walkTopLevelNodes(nodes);
    }
}
