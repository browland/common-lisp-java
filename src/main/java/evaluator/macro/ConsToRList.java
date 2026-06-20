package evaluator.macro;

import syntaxtree.Node;
import syntaxtree.RList;
import value.*;

public class ConsToRList {
    public Node translate(ConsCellValue consCellValue) {
        RList rList = new RList();

        ConsCell nextConsCell = consCellValue.getValue();

        while(nextConsCell != null) {
            Node node = extractNode(nextConsCell);
            rList.add(node);

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

        return rList;
    }

    private Node extractNode(ConsCell nextConsCell) {
        Value<?> carValue = nextConsCell.car();
        return switch(carValue) {
            case StringValue stringValue -> ValueToAtom.toAtom(stringValue);
            case IntegerValue integerValue -> ValueToAtom.toAtom(integerValue);
            case SymbolValue symbolValue -> ValueToAtom.toAtom(symbolValue);
            case ConsCellValue consCellValue -> translate(consCellValue);
            case ClosureValue closureValue -> ClosureToLambda.toLambdaNode(closureValue);
            default -> throw new IllegalStateException("Unhandled value in cons: " + carValue);
        };
    }
}
