package evaluator;

import function.Closure;
import function.Function;
import function.FunctionRegistry;
import reader.*;
import syntaxtree.Atom;
import syntaxtree.Node;
import syntaxtree.RList;
import syntaxtree.SyntaxTreeBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Evaluator {
    private static final Set<String> SPECIAL_FORM_OPERATORS = Set.of("lambda");

    private final FunctionRegistry functionRegistry = new FunctionRegistry();

    public static void main(String[] args) {
        String program = "(( (lambda (x) (lambda (y) (+ x y))) 10) 5)";
        Evaluator e = new Evaluator();
        System.out.println(e.evaluate(program, new HashMap<>()));
    }

    public Value<?> evaluate(String program, Map<String,String> environment) {
        SyntaxTreeBuilder syntaxTreeBuilder = new SyntaxTreeBuilder();
        CharacterReader characterReader = new CharacterReader(syntaxTreeBuilder);
        characterReader.read(program);

        RList list = syntaxTreeBuilder.getResult();
        return evaluate(list, environment);
    }

    public Value<?> evaluate(RList list, Map<String,String> environment) {
        Function operator;
        Node operatorNode = list.get(0);

        if (operatorNode instanceof RList) {
            Value<?> evaluatedOperatorValue = evaluate((RList)operatorNode, environment);
            operator = (Function)evaluatedOperatorValue.value();

            // it's always a regular form when the operator is a list.  Special forms are only legal when the operator
            // is a predefined value.
            return applyForm(operator, list, environment);
        } else {
            Atom operatorAtom = (Atom) operatorNode;
            // only lambda special form handled so far
            if(SPECIAL_FORM_OPERATORS.contains(operatorAtom.value())) {
                operator = evaluateLambda(list, environment);
                return new Value<>(operator, ValueType.OPERATOR);
            }
            else {
                // it's a regular form - operator and operands - we know enough to handle it here
                operator = functionRegistry.findByName(operatorAtom.value());
                return applyForm(operator, list, environment);
            }
        }
    }

    private Value<?> applyForm(Function operator, RList fullList, Map<String,String> environment) {
        List<String> operands = fullList.nodes().subList(1, fullList.size()).stream()
                .map(node -> {
                    Atom atom = (Atom) node;
                    return atom.value();
                }).toList();
        return operator.apply(operands, environment);
    }

    private Closure evaluateLambda(RList list, Map<String,String> capturedEnvironment) {
        Node operator = list.get(0);
        if(operator instanceof RList) {
            throw new IllegalStateException("should not get here - need some better handling?");
        }

        RList bindingsList = (RList)list.get(1);
        List<String> bindings = bindingsList.nodes().stream().map(node -> {
            Atom atom = (Atom)node;
            return atom.value();
        }).toList();

        return new Closure(this, capturedEnvironment, bindings, (RList)list.get(2));
    }
}
