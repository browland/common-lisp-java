public enum ArgType {
    NUMERIC, EXPRESSION, STRING, BOOLEAN, BINDING;

    static ArgType ofArg(String argument) {
        if(argument.startsWith("(")) {
            return ArgType.EXPRESSION;
        }
        if(argument.startsWith("\"")) {
            return ArgType.STRING;
        }
        if("true".equals(argument) || "false".equals(argument)) {
            return ArgType.BOOLEAN;
        }

        try {
            Integer.parseInt(argument);
            return ArgType.NUMERIC;
        }
        catch(NumberFormatException e) {
        }

        // todo assume for now it's a binding
        return ArgType.BINDING;
    }
}
