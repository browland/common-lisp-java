import java.util.Arrays;
import java.util.Objects;

public class Interpreter {
    public static void main(String[] args) {
        String program = """
                (if (eq (add 1 2 1) (add 2 2)) 1 2)
                """;
        System.out.println("result: " + eval(program));
    }

    static String eval(String program) {
        String[] expressions = Parser.extractExpressionsAtThisLevel(program);

        String function = expressions[0];
        String[] arguments = Arrays.copyOfRange(expressions, 1, expressions.length);

        return switch(function) {
            case "add" -> addFunction(arguments);
            case "eq" -> eqFunction(arguments);
            case "if" -> ifFunction(arguments);
            default -> throw new IllegalArgumentException("Unknown function " + function);
        };
    }

    private static String addFunction(String[] arguments) {
        String[] evaldArgs = evalArgs(arguments);
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

        String[] evaldArgs = evalArgs(arguments);

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

        String[] evaldArgs = evalArgs(arguments);

        if(ArgType.ofArg(evaldArgs[0]) != ArgType.BOOLEAN) {
            throw new IllegalArgumentException("first arg must be boolean for if");
        }

        return Boolean.parseBoolean(evaldArgs[0]) ? evaldArgs[1] : evaldArgs[2];
    }

    private static String[] evalArgs(String[] arguments) {
        return Arrays.stream(arguments)
                .map(arg -> {
                    if(ArgType.EXPRESSION == ArgType.ofArg(arg)){
                        return eval(arg);
                    }
                    else {
                        return arg;
                    }
                }).toArray(String[]::new);
    }
}
