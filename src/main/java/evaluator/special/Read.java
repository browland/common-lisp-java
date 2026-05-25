package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import evaluator.env.Symbols;
import syntaxtree.RList;
import value.*;

import java.util.Optional;
import java.util.Scanner;

public class Read implements SpecialForm {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public Value<?> evaluate(RList entireList, Environment environment, Evaluator evaluator) {
        String input = scanner.next();

        // todo not wanted
        // determine type based on what was entered
        Symbol possibleSymbol = Symbols.internSymbol(input);
        Optional<Value<?>> lookedUpSymbolOptional = environment.getVariable(possibleSymbol);
        if(lookedUpSymbolOptional.isPresent()){
            return new SymbolValue(possibleSymbol);
        }

        try {
            int intValue = Integer.parseInt(input);
            return new IntegerValue(intValue);
        } catch (NumberFormatException e) {
            return new StringValue(input);
        }
    }
}
