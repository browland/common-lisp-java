package evaluator.macro;

import syntaxtree.Node;
import syntaxtree.NodeBuilder;
import syntaxtree.RList;
import value.*;

public class ConsToRList {
    public Node translate(ConsCellValue consCellValue) {
        return translateI(consCellValue).build();
    }

    public NodeBuilder translateI(ConsCellValue consCellValue) {
        RList.Builder rListBuilder = new RList.Builder();
        ConsCell nextConsCell = consCellValue.getValue();

        while(nextConsCell != null) {
            NodeBuilder nodeBuilder = extractBuilderForThisNode(nextConsCell);
            rListBuilder.addNodeBuilder(nodeBuilder);

            Value<?> nextConsCellValue = nextConsCell.cdr();
            if(nextConsCellValue.equals(Value.nil())) {
                nextConsCell = null;
            }
            else {
                if(nextConsCellValue instanceof ConsCellValue possibleConsCellValue) {
                    nextConsCell = possibleConsCellValue.getValue();
                }
                else {
                    throw new IllegalStateException("Some other value found within cons cell structure: " + nextConsCellValue);
                }
            }
        }

        return rListBuilder;
    }

    private NodeBuilder extractBuilderForThisNode(ConsCell nextConsCell) {
        Value<?> carValue = nextConsCell.car();
        NodeBuilder nodeBuilder;
        if(carValue instanceof StringValue stringValue) {
            nodeBuilder = ValueToAtomBuilder.atomBuilder(stringValue);
        }
        else if(carValue instanceof IntegerValue integerValue) {
            nodeBuilder = ValueToAtomBuilder.atomBuilder(integerValue);
        }
        else if(carValue instanceof SymbolValue symbolValue) {
            nodeBuilder = ValueToAtomBuilder.atomBuilder(symbolValue);
        }
        else if(carValue instanceof ConsCellValue consCellValue) {
            nodeBuilder = translateI(consCellValue);
        }
        else {
            throw new IllegalStateException("Unhandled value in cons: " + carValue);
        }
        return nodeBuilder;
    }

}
