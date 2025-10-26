import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Interpreter {
    public static void main(String[] args) {
        String program = """
                (if (eq (add 1 2 1) (add 2 2)) 1 2)
                """;
        System.out.println("result: " + eval(program, Map.of()));
    }

    /**
     * Entrypoint - expects the top-level program, no bindings
     */
    static String eval(String program) {
        String[] topLevelForms = Parser.extractTopLevelForms(program);
        String result = null;
        for(String form : topLevelForms) {
            result = eval(form, Map.of());
        }
        return result;
    }

    static String eval(String program, Map<String,String> bindings) {
        System.out.println("eval: " + program);
        String[] expressions = Parser.splitExpressionsAtThisLevel(program);

        if(expressions.length == 0) {
            // nothing needed splitting out as 'program' was just a single atom. So we just evaluate it.
            return evalArgs(new String[]{program}, bindings)[0];
        }

        String function = expressions[0];
        String[] arguments = Arrays.copyOfRange(expressions, 1, expressions.length);

        if(Parser.isLambda(function)) {
            return lambdaFunction(function, arguments, bindings);
        }

        return switch(function) {
            case "add", "+" -> addFunction(arguments, bindings);
            case "eq" -> eqFunction(arguments);
            case "if" -> ifFunction(arguments, bindings);
            case "let" -> letFunction(arguments, bindings);
            case "less" -> lessFunction(arguments, bindings);
            default -> throw new IllegalArgumentException("Unknown function " + function);
        };
    }


    private static String addFunction(String[] arguments, Map<String,String> bindings) {
        String[] evaldArgs = evalArgs(arguments, bindings);
        Integer[] intArgs = Arrays.stream(evaldArgs)
                .map(Integer::parseInt)
                .toArray(Integer[]::new);

        int sum = Arrays.stream(intArgs).mapToInt(Integer::intValue).sum();
        return Integer.toString(sum);
    }

    private static String eqFunction(String[] arguments) {
        if(arguments.length != 2) {
            throw new IllegalArgumentException("must be 2 args for eq");
        }

        String[] evaldArgs = evalArgs(arguments, Map.of());

        if(ArgType.ofArg(evaldArgs[0]) != ArgType.ofArg(evaldArgs[1])) {
            throw new IllegalArgumentException("args must be same types for eq");
        }

        ArgType type = ArgType.ofArg(evaldArgs[0]);
        if(Objects.requireNonNull(type) == ArgType.NUMERIC) {
            int arg1Value = Integer.parseInt(evaldArgs[0]);
            int arg2Value = Integer.parseInt(evaldArgs[1]);
            return (arg1Value == arg2Value) ? "true" : "false";
        }

        throw new IllegalArgumentException("args must be numeric for now, for eq");
    }

    private static String lessFunction(String[] arguments, Map<String,String> bindings) {
        if(arguments.length != 2) {
            throw new IllegalArgumentException("must be 2 args for less");
        }

        String[] evaldArgs = evalArgs(arguments, bindings);

        if(ArgType.ofArg(evaldArgs[0]) != ArgType.ofArg(evaldArgs[1])) {
            throw new IllegalArgumentException("args must be same types for less");
        }

        ArgType type = ArgType.ofArg(evaldArgs[0]);
        if(Objects.requireNonNull(type) == ArgType.NUMERIC) {
            int arg1Value = Integer.parseInt(evaldArgs[0]);
            int arg2Value = Integer.parseInt(evaldArgs[1]);
            return (arg1Value < arg2Value) ? "true" : "false";
        }

        throw new IllegalArgumentException("args must be numeric for now, for less");

    }

    private static String ifFunction(String[] arguments, Map<String,String> bindings) {
        if(arguments.length != 3) {
            throw new IllegalArgumentException("must be 3 args for if");
        }

        String[] evaldArgs = evalArgs(arguments, bindings);

        if(ArgType.ofArg(evaldArgs[0]) != ArgType.BOOLEAN) {
            throw new IllegalArgumentException("first arg must be boolean for if");
        }

        return Boolean.parseBoolean(evaldArgs[0]) ? evaldArgs[1] : evaldArgs[2];
    }

    private static String letFunction(String[] arguments, Map<String,String> enclosingBindings) {
        if(arguments.length < 2) {
            throw new IllegalArgumentException("must be at least 2 args for let");
        }

        // extract bindings at this level; add in to enclosingBindings
        String bindingsList = arguments[0];
        String[] bindingsSeparated = Parser.splitExpressionsAtThisLevel(bindingsList);
        Map<String,String> bindingsAtThisLevelPlusEnclosingBindings = new HashMap<>(Map.copyOf(enclosingBindings));

        for(String binding : bindingsSeparated) {
            String[] variableAndValue = Parser.splitExpressionsAtThisLevel(binding);
            // Value may be itself an expression!
            String value = variableAndValue[1];
            String valueEvaluated = eval(value, bindingsAtThisLevelPlusEnclosingBindings);
            bindingsAtThisLevelPlusEnclosingBindings.put(variableAndValue[0], valueEvaluated);
        }

        // evaluate each form and return the last one
        // evaluating forms prior to the last one seems pointless but makes sense where there are side-effects
        String[] forms = Arrays.copyOfRange(arguments, 1, arguments.length);
        String result = null;
        for(String form : forms) {
            result = eval(form, bindingsAtThisLevelPlusEnclosingBindings);
        }
        return result;
    }

    private static String lambdaFunction(String definition, String[] arguments, Map<String,String> enclosingBindings) {
        // todo there could be more arguments to the lambda, not just one
        // we'll start assuming it's a single-level lambda.  Soon we'll need to detect another level of nesting and recursively call in with the deeper expression
        String[] partsOfDefinition = Parser.splitExpressionsAtThisLevel(definition);
        // if this is a nested lambda then the first part will be more complex than just 'lambda', we need to evaluate it further
        if(!"lambda".equals(partsOfDefinition[0])) {
            return lambdaFunction(partsOfDefinition[0], arguments, enclosingBindings);

        }
        String argsList = partsOfDefinition[1];
        String expressionToEvaluate = partsOfDefinition[2];

        // we expect args to be the bindings for argsList
        String[] argNames = Parser.splitExpressionsAtThisLevel(argsList);

        Map<String,String> bindings = new HashMap<>();

        // todo bear in mind there may be arguments not bound to a value yet!  We need to store them but represent we don't have a binding yet.
        for(int i = 0; i<argNames.length; i++) {
            bindings.put(argNames[i], arguments[i]);
        }

        // add bindings at this level into enclosingBindings
        Map<String,String> bindingsAtThisLevelPlusEnclosingBindings = new HashMap<>(Map.copyOf(enclosingBindings));
        bindingsAtThisLevelPlusEnclosingBindings.putAll(bindings);

        // todo bear in mind the result returned here could be a new function, e.g. a lambda with some bindings but not all
        //      this happens if we're evaluating an 'inner' lambda - and the outer one will bind a remaining argument to this one.

        // todo is it worth changing the return type to be something else so I can pass functions around?  Clearly there's no avoiding that now
        //      (beta reduction is not enough)
        //      This would mean first changing tests to e.g. expect an int result type from an add operation for example
        return eval(expressionToEvaluate, bindingsAtThisLevelPlusEnclosingBindings);
    }

    private static String[] evalArgs(String[] arguments,
                                     Map<String,String> bindings) {
        return Arrays.stream(arguments)
                .map(arg -> {
                    ArgType argType = ArgType.ofArg(arg);
                    if(ArgType.EXPRESSION == argType){
                        return eval(arg, bindings);
                    }
                    else if(ArgType.BINDING == argType) {
                        String evaluated = bindings.get(arg);
                        if(evaluated == null) {
                            throw new UndefinedVariableException("Undefined variable " + arg);
                        }
                        return evaluated;
                    }
                    else {
                        return arg;
                    }
                }).toArray(String[]::new);
    }
}
