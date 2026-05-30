package function;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import printing.StringFormatter;
import value.Symbol;
import value.SymbolValue;
import value.Value;

import java.util.List;
import java.util.Set;

public class Format implements Function {
    private static final Symbol T = Symbols.internSymbol("t");
    private static final Symbol NIL = Symbols.internSymbol("nil");

    private final Set<Symbol> SUPPORTED_STREAMS = Set.of(
            T,
            NIL
    );

    @Override
    public Value<?> apply(List<Value<?>> operands, Environment environment) {
        // The first operand is the output stream to send the string, which is the second operand.
        Value<?> streamValue = operands.get(0);
        String formatString = operands.get(1).getValue().toString();
        List<Value<?>> values = operands.subList(2, operands.size());

        // t is a built-in symbol for constant logical true.  Correct CL behaviour is to treat t as meaning stdout.
        if(streamValue instanceof SymbolValue streamSymbolValue) {
            Symbol streamSymbol = streamSymbolValue.getValue();
            if(!SUPPORTED_STREAMS.contains(streamSymbol)) {
                throw new UnsupportedOperationException("Unsupported stream symbol: " + streamSymbol);
            }

            if(T.equals(streamSymbol)) {
                // print value to standard out; return nil
                String formatted = StringFormatter.format(formatString, values);
                System.out.println(formatted);
                return Value.nil();
            }
            else if(NIL.equals(streamSymbol)) {
                return operands.get(1);
            }
            else {
                throw new UnsupportedOperationException("Unsupported builtin stream constant after evaluation " + streamValue);
            }
        }
        else {
            throw new UnsupportedOperationException("Unsupported stream after evaluation " + streamValue);
        }
    }
}
