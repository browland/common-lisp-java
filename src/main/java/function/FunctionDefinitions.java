package function;

import evaluator.env.Environment;
import evaluator.env.Symbols;
import value.FunctionValue;

public class FunctionDefinitions {
    public static void addFunctionDefinitions(Environment env) {
        env.setFunction(Symbols.internSymbol("+"), new FunctionValue(new Add()));
        env.setFunction(Symbols.internSymbol("add"), new FunctionValue(new Add()));
        env.setFunction(Symbols.internSymbol("subtract"), new FunctionValue(new Subtract()));
        env.setFunction(Symbols.internSymbol("-"), new FunctionValue(new Subtract()));
        env.setFunction(Symbols.internSymbol("and"), new FunctionValue(new And()));
        env.setFunction(Symbols.internSymbol("eq"), new FunctionValue(new Eq()));
        env.setFunction(Symbols.internSymbol("*"), new FunctionValue(new Multiply()));
        env.setFunction(Symbols.internSymbol("format"), new FunctionValue(new Format()));
        env.setFunction(Symbols.internSymbol("load"), new FunctionValue(new Load()));
        env.setFunction(Symbols.internSymbol("list"), new FunctionValue(new ListFunction()));
        env.setFunction(Symbols.internSymbol("getf"), new FunctionValue(new GetF()));
        env.setFunction(Symbols.internSymbol("cons"), new FunctionValue(new Cons()));
        env.setFunction(Symbols.internSymbol("consp"), new FunctionValue(new Consp()));
        env.setFunction(Symbols.internSymbol("car"), new FunctionValue(new Car()));
        env.setFunction(Symbols.internSymbol("cdr"), new FunctionValue(new Cdr()));
        env.setFunction(Symbols.internSymbol("cadr"), new FunctionValue(new Cadr()));
        env.setFunction(Symbols.internSymbol("="), new FunctionValue(new NumsEqual()));
        env.setFunction(Symbols.internSymbol("<"), new FunctionValue(new LessThan()));
        env.setFunction(Symbols.internSymbol(">"), new FunctionValue(new GreaterThan()));
        env.setFunction(Symbols.internSymbol("macroexpand-1"), new FunctionValue(new Macroexpand1()));
        env.setFunction(Symbols.internSymbol("rplaca"), new FunctionValue(new RPlaca()));
        env.setFunction(Symbols.internSymbol("symbolp"), new FunctionValue(new Symbolp()));
        env.setFunction(Symbols.internSymbol("listp"), new FunctionValue(new Listp()));
        env.setFunction(Symbols.internSymbol("funcall"), new FunctionValue(new Funcall()));
        env.setFunction(Symbols.internSymbol("random"), new FunctionValue(new Random()));
        env.setFunction(Symbols.internSymbol("assoc"), new FunctionValue(new Assoc()));
        env.setFunction(Symbols.internSymbol("null"), new FunctionValue(new Null()));
        env.setFunction(Symbols.internSymbol("string="), new FunctionValue(new StringEqual()));
        env.setFunction(Symbols.internSymbol("evenp"), new FunctionValue(new Evenp()));
        env.setFunction(Symbols.internSymbol("values"), new FunctionValue(new Values()));
        env.setFunction(Symbols.internSymbol("floor"), new FunctionValue(new Floor()));
        env.setFunction(Symbols.internSymbol("open"), new FunctionValue(new Open()));
        env.setFunction(Symbols.internSymbol("close"), new FunctionValue(new Close()));
        env.setFunction(Symbols.internSymbol("read-char"), new FunctionValue(new ReadChar()));
        env.setFunction(Symbols.internSymbol("gensym"), new FunctionValue(new Gensym()));
    }
}
