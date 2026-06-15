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
        Node node;
        if(carValue instanceof StringValue stringValue) {
            node = ValueToAtom.toAtom(stringValue);
        }
        else if(carValue instanceof IntegerValue integerValue) {
            node = ValueToAtom.toAtom(integerValue);
        }
        else if(carValue instanceof SymbolValue symbolValue) {
            node = ValueToAtom.toAtom(symbolValue);
        }
        else if(carValue instanceof ConsCellValue consCellValue) {
            node = translate(consCellValue);
        }
        else if(carValue instanceof ClosureValue closureValue) {
            node = ClosureToLambda.toLambdaNode(closureValue);
        }
        else {
            // todo this happens when running 3-lists.lisp - this is because the offending value is buried in a ConsCellValue
            //      so we dive into cons cell value handling and fail when handling the values within the cons cell
            //      We need to rip out usage of builders, and re-use ClosureToLambda.
            throw new IllegalStateException("Unhandled value in cons: " + carValue);
        }
        return node;
    }

}
