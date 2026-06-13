package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import value.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Works in a similar way to quote (returns the syntax tree operand as a value), except we also
 * allow the unquote operator, which causes the operand to be evaluated.  So the unquote operator is
 * not exposed generally, we only interpret it at this level (when expanding the operand of
 * the quasiquote operator).
 */
public class Quasiquote implements SpecialForm {
    @Override
    public Value<?> evaluate(RList entireList,
                             Environment environment,
                             Evaluator evaluator) {
        // the single operand is either a list or an atom
        Node operand = entireList.nodes().get(1);

        // Each node can be either an atom or a list.  We keep track of whether we're unquoting the next node (and reset
        // state after).
        if(operand instanceof RList rlist) {
            return handleRList(rlist, environment, evaluator).evaluatedValue();
        }
        else if(operand instanceof Atom atom) {
            return Value.of(atom.value());
        }
        else {
            throw new UnsupportedOperationException("Unhandled type for quasiquote " + operand);

        }
    }

    /**
     * This can return either a list representing the value of the syntax tree list passed in, OR it can return a
     * single value if the syntax tree passed in represents the unquote of a symbol.
     */
    private QuasiquoteResult handleRList(RList rlist,
                                         Environment env,
                                         Evaluator evaluator) {
        // Handle case of empty list first
        if(rlist.nodes().isEmpty()) {
            return new QuasiquoteResult(new ConsCellValue(new ConsCell(Value.nil(), Value.nil())), false);
        }

        Node firstNode = rlist.get(0);
        if(firstNode instanceof Atom firstAtom) {
            String value = firstAtom.value();
            if("unquote".equals(value) || "unquote-splicing".equals(value)) {
                // get the enclosed form and return its evaluation
                Node unquotedForm = rlist.get(1);
                Value<?> evaluated = evaluator.evaluate(unquotedForm, env);
                boolean splicing = "unquote-splicing".equals(value);
                return new QuasiquoteResult(evaluated, splicing);
            }
        }

        // otherwise this is some other list, walk the structure and handle atom-by-atom or by recursive calls for list forms

        ConsCell currentCons = null;

        // We walk the nodes in reverse order so that the cons structure has a 'car' of the first element in the list.
        // We'd have to reverse at some point; may as well do it at the outset so the source of truth is correct
        for (Node node : rlist.nodes().reversed()) {
            if(node instanceof Atom atom) {
                Value<?> result = atomToValueNoEvaluation(atom);
                currentCons = pushToCons(result, currentCons);
            }
            else if(node instanceof RList rlistNode) {
                QuasiquoteResult unquoteResult = handleRList(rlistNode, env, evaluator);  // can be atom or list; if was splicing then add elems one by one to current cons
                if(!unquoteResult.unquoteSplicing()) {
                    currentCons = pushToCons(unquoteResult.evaluatedValue(), currentCons);
                }
                else {
                    // step over values in the evaluated result and splice them in to current cons element by element
                    Value<?> evaluatedResult = unquoteResult.evaluatedValue();
                    if(evaluatedResult.getType() == ValueType.CONS_CELL) {
                        List<Value<?>> tempList = new ArrayList<>();
                        ConsCellValue evaluatedConsCellValue = (ConsCellValue) evaluatedResult;
                        ConsCell evaluatedCons = evaluatedConsCellValue.getValue();
                        for(Value<?> currentVal : evaluatedCons) {
                            tempList.add(currentVal);
                        }

                        // We need the temp list reversed, as otherwise copying one cons directly to another will end up reversing the order!
                        for(Value<?> currentVal : tempList.reversed()) {
                            currentCons = pushToCons(currentVal, currentCons);
                        }
                    }
                    else {
                        currentCons = pushToCons(evaluatedResult, currentCons);
                    }
                }
            }
        }

        return new QuasiquoteResult(new ConsCellValue(currentCons), false);
    }

    public Value<?> atomToValueNoEvaluation(Atom atom) {
        String atomStringValue = atom.value();
        if(atomStringValue.startsWith("\"") && atomStringValue.endsWith("\"")) {
            String stringWithoutQuotes = atomStringValue.substring(1, atomStringValue.length()-1);
            return new StringValue(stringWithoutQuotes);
        }
        else if(isNumeric(atomStringValue)) {
            int intValue = Integer.parseInt(atomStringValue);
            return new IntegerValue(intValue);
        }
        else {
            // treat as symbol
            Symbol symbol = Symbols.internSymbol(atomStringValue);
            return new SymbolValue(symbol);
        }
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private ConsCell pushToCons(Value<?> value, ConsCell currentCons) {
        Value<?> cdr = currentCons == null ? Value.nil() : new ConsCellValue(currentCons);
        return new ConsCell(value, cdr);
    }

    record QuasiquoteResult(Value<?> evaluatedValue, boolean unquoteSplicing) {
    }
}
