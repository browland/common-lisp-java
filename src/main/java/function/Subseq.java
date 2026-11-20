package function;

import evaluator.env.Environment;
import exception.EvaluationException;
import value.IntegerValue;
import value.StringValue;
import value.Value;

import java.util.List;

/**
 * This should operate on vectors, but for now is just hardcoded to strings, which are themselves a monolithic type.
 */
public class Subseq implements Function {
    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // Works like Java's substring().
        // Can have 2 or 3 operands (string, start index, optional end index)
        if (operands.size() < 2 || operands.size() > 3) {
            throw new EvaluationException("subseq: incorrect operands: " + operands);
        }

        StringValue stringValue = StringValue.expectStringValue(operands.getFirst(), "subseq");
        String string = stringValue.getValue();

        IntegerValue startOffsetValue = IntegerValue.expectIntegerValue(operands.get(1), "subseq");
        int startOffset = startOffsetValue.getValue();

        IntegerValue endOffsetValue;
        int endOffset = -1;
        if (operands.size() == 3) {
            endOffsetValue = IntegerValue.expectIntegerValue(operands.get(2), "subseq");
            endOffset = endOffsetValue.getValue();
        }
        else {
            endOffset = string.length();
        }

        if (endOffset < startOffset || startOffset < 0 || endOffset > string.length()) {
            throw new EvaluationException("subseq: bounds check failure, operands: " + operands);
        }

        String substr = string.substring(startOffset, endOffset);
        return new StringValue(substr);
    }
}
