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

    static String eval(String program, Map<String,String> bindings) {
        System.out.println("eval: " + program);
        String[] expressions = Parser.splitExpressionsAtThisLevel(program);

        if(expressions.length == 0) {
            // nothing needed splitting out as 'program' was just a single atom. So we just evaluate it.
            return evalArgs(new String[]{program}, bindings)[0];
        }

        String function = expressions[0];
        String[] arguments = Arrays.copyOfRange(expressions, 1, expressions.length);

        return switch(function) {
            case "add" -> addFunction(arguments, bindings);
            case "eq" -> eqFunction(arguments);
            case "if" -> ifFunction(arguments);
            case "let" -> letFunction(arguments);
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

    private static String ifFunction(String[] arguments) {
        if(arguments.length != 3) {
            throw new IllegalArgumentException("must be 3 args for if");
        }

        String[] evaldArgs = evalArgs(arguments, Map.of());

        if(ArgType.ofArg(evaldArgs[0]) != ArgType.BOOLEAN) {
            throw new IllegalArgumentException("first arg must be boolean for if");
        }

        return Boolean.parseBoolean(evaldArgs[0]) ? evaldArgs[1] : evaldArgs[2];
    }

    private static String letFunction(String[] arguments) {
        if(arguments.length != 2) {
            throw new IllegalArgumentException("must be 2 args for let");
        }

        // maintain bindings
        String[] bindingsSeparated = Parser.splitExpressionsAtThisLevel(arguments[0]);
        Map<String,String> bindingsMap = new HashMap<>();

        for(String binding : bindingsSeparated) {
            String[] variableAndValue = Parser.splitExpressionsAtThisLevel(binding);
            bindingsMap.put(variableAndValue[0], variableAndValue[1]);
        }

        // evaluate form
        return eval(arguments[1], bindingsMap);
    }

    private static String[] evalArgs(String[] arguments,
                                     Map<String,String> bindings) {
        return Arrays.stream(arguments)
                .map(arg -> {
                    ArgType argType = ArgType.ofArg(arg);
                    if(ArgType.EXPRESSION == argType){
                        return eval(arg, Map.of());
                    }
                    else if(ArgType.BINDING == argType) {
                        return bindings.get(arg);
                    }
                    else {
                        return arg;
                    }
                }).toArray(String[]::new);
    }
}
