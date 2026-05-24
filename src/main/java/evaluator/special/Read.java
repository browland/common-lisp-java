package evaluator.special;

import evaluator.Evaluator;
import evaluator.env.Environment;
import syntaxtree.RList;
import value.IntegerValue;
import value.StringValue;
import value.Value;

import java.util.Scanner;

public class Read implements SpecialForm {
    private Scanner scanner = new Scanner(System.in);

    @Override
    public Value<?> evaluate(RList entireList, Environment environment, Evaluator evaluator) {
        String input = scanner.next();

        // determine type based on what was entered
        try {
            int intValue = Integer.parseInt(input);
            return new IntegerValue(intValue);
        } catch (NumberFormatException e) {
            return new StringValue(input);
        }
    }
}
