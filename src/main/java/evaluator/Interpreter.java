package evaluator;

import evaluator.env.Environment;
import reader.CharacterReader;
import syntaxtree.ParseElementBuilder;
import syntaxtree.RList;
import syntaxtree.SyntaxTreeBuilder;
import value.Value;

public class Interpreter {
    private final Evaluator evaluator;
    private final CharacterReader characterReader;
    private final SyntaxTreeBuilder syntaxTreeBuilder;
    private final Environment environment;

    public Interpreter() {
        this(new Environment());
    }

    public Interpreter(Environment environment) {
        syntaxTreeBuilder = new SyntaxTreeBuilder();
        ParseElementBuilder parseElementBuilder = new ParseElementBuilder(syntaxTreeBuilder);
        characterReader = new CharacterReader(parseElementBuilder);

        this.evaluator =  new Evaluator();
        this.environment = environment;
    }

    public Value<?> interpret(String program) {
        characterReader.read(program);

        RList list = syntaxTreeBuilder.getResult();
        Value<?> evaluate = evaluator.evaluate(list, environment);
        syntaxTreeBuilder.reset();
        return evaluate;
    }
}
