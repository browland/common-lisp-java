package evaluator;

import evaluator.special.SpecialFormEvaluator;
import function.*;
import reader.CharacterReader;
import syntaxtree.*;
import value.Value;
import value.ValueType;

import java.util.*;

public class Evaluator {
    private static final Set<String> BUILTIN_CONSTANTS = Set.of("t", "nil");

    private final FunctionRegistry functionRegistry = new FunctionRegistry();
    private final SpecialFormEvaluator specialFormEvaluator = new SpecialFormEvaluator();

//    public static void main(String[] args) {
////        String program = "(defvar *db* nil)";
//        Evaluator e = new Evaluator();
//        System.out.println(e.evaluate(program, new HashMap<>()));
//    }

    public Value<?> evaluate(String program, Map<String,Value<?>> environment) {
        SyntaxTreeBuilder syntaxTreeBuilder = new SyntaxTreeBuilder();
        CharacterReader characterReader = new CharacterReader(syntaxTreeBuilder);
        characterReader.read(program);

        RList list = syntaxTreeBuilder.getResult();
        return evaluate(list, environment);
    }

    public Value<?> evaluate(RList list, Map<String,Value<?>> environment) {
        Function operator;
        Node operatorNode = list.get(0);

        // If the operator is itself a list, then we need to evaluate it to get a closure.
        // We can then apply it to the remaining arguments as a form.
        if (operatorNode instanceof RList) {
            Value<?> evaluatedOperatorValue = evaluate((RList)operatorNode, environment);
            operator = (Function)evaluatedOperatorValue.value();

            // it's always a regular form when the operator is a list.  Special forms are only legal when the operator
            // is a predefined value.
            return applyForm(operator, list, environment);
        } else {
            // If the operator is a special form we need to evaluate it to get its operator implementation (e.g. a closure for a
            // lambda definition).
            // todo: maybe some special forms return a straightforward result when eval'd e.g. defvar, so not always a
            //       recursive call and don't need to treat all special forms as functions/operators?
            Atom operatorAtom = (Atom) operatorNode;
            Optional<Value<?>> optionalSpecialFormResult =
                    specialFormEvaluator.evaluate(operatorAtom.value(), list, environment, this);
            if(optionalSpecialFormResult.isPresent()) {
                return optionalSpecialFormResult.get();
            }

            // We did not match on any special form, so this must be a regular form.
            // We know enough to handle it here.
            operator = functionRegistry.findByName(operatorAtom.value());
            if (operator == null) {
                Value<?> possibleStoredOperator = environment.get(operatorAtom.value());
                if(possibleStoredOperator == null) {
                    System.out.println("ERROR: Can't find definition for operator " + operatorAtom.value());
                    System.exit(-1);
                }

                if (ValueType.OPERATOR == possibleStoredOperator.type()) {
                    operator = (Function) possibleStoredOperator.value();
                }
            }
            if (operator == null) {
                throw new IllegalArgumentException("Could not find operator " + operatorAtom);
            }
            return applyForm(operator, list, environment);
        }
    }

    private Value<?> applyForm(Function operator, RList fullList, Map<String,Value<?>> environment) {
        List<? extends Value<?>> operands = fullList.nodes().subList(1, fullList.size()).stream()
                .map(node -> evaluate(node, environment)).toList();
        return operator.apply((List<Value<?>>) operands, environment);
    }

    public Value<?> evaluate(Node node, Map<String,Value<?>> environment) {
        // either an atom or a list
        // if an atom then it could be a symbol (in which case look it up) or otherwise a literal value
        // if a list, then pass it back through evaluate() with the environment
        if(node instanceof Atom atom) {
            return atomToValue(atom, environment);
        }
        else if (node instanceof RList rlist) {
            return evaluate(rlist, environment);
        }
        else {
            throw new UnsupportedOperationException("Unhandled node type");
        }
    }

    private Value<?> atomToValue(Atom atom, Map<String,Value<?>> environment) {
        String atomStringValue = atom.value();
        if(BUILTIN_CONSTANTS.contains(atomStringValue)) {
            return new Value<>(atomStringValue, ValueType.BUILTIN_CONSTANT);
        }
        else if(QuoteType.STRING == atom.quoteType()) {
            return new Value<>(atomStringValue, ValueType.STRING_LITERAL);
        }
        else if(QuoteType.KEYWORD == atom.quoteType()) {
            return new Value<>(atomStringValue, ValueType.KEYWORD);
        }
        else {
            // could be in the environment; otherwise fall back to int
            if(environment.containsKey(atomStringValue)) {
                return environment.get(atomStringValue);
            }

            int intValue = Integer.parseInt(atomStringValue);
            return new Value<>(intValue, ValueType.INTEGER_LITERAL);
        }
    }
}
