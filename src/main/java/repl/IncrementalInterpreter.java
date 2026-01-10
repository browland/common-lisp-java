package repl;

import evaluator.Evaluator;
import evaluator.env.Environment;
import reader.CharacterReader;
import syntaxtree.RList;
import syntaxtree.ParseElementBuilder;
import syntaxtree.SyntaxTreeBuilder;
import value.Value;

public class IncrementalInterpreter {
    private final SyntaxTreeBuilder syntaxTreeBuilder;
    private final ParseElementBuilder parseElementBuilder;
    private final CharacterReader characterReader;
    private final Evaluator evaluator;
    private final Environment environment;
    private final ReplOutput replOutput;

    // For now it's convenient to ensure we've got a singleton instance and easily acquired, e.g. by load() function.
    public static IncrementalInterpreter INSTANCE;

    public IncrementalInterpreter(SyntaxTreeBuilder syntaxTreeBuilder,
                                  ParseElementBuilder parseElementBuilder,
                                  CharacterReader characterReader,
                                  Evaluator evaluator,
                                  Environment environment,
                                  ReplOutput replOutput) {
        this.syntaxTreeBuilder = syntaxTreeBuilder;
        this.parseElementBuilder = parseElementBuilder;
        this.characterReader = characterReader;
        this.evaluator = evaluator;
        this.environment = environment;
        this.replOutput = replOutput;

        INSTANCE = this;
    }

    public void consume(char c) {
        characterReader.consume(c);

        if (syntaxTreeBuilder.isFinished()) {
            Value<?> value = evaluateExpression(syntaxTreeBuilder, environment, evaluator);
            replOutput.emitOutput(value.toString());
        } else {
            if (c == '\n') {
                if(syntaxTreeBuilder.isEmpty()) {
                    replOutput.promptForNewForm();
                }
                else {
                    replOutput.promptForMidForm();
                }
            }
        }
    }

    private Value<?> evaluateExpression(SyntaxTreeBuilder syntaxTreeBuilder,
                                    Environment environment,
                                    Evaluator evaluator) {
        RList topLevelList = syntaxTreeBuilder.getResult();
        parseElementBuilder.reset();
        return evaluator.evaluate(topLevelList, environment);
    }
}
