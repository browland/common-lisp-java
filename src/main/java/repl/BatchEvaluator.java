package repl;

import evaluator.Evaluator;
import evaluator.env.Environment;
import reader.CharacterReader;
import syntaxtree.RList;
import syntaxtree.ParseElementBuilder;
import syntaxtree.SyntaxTreeBuilder;
import value.Value;

public class BatchEvaluator {
    private final SyntaxTreeBuilder syntaxTreeBuilder;
    private final ParseElementBuilder parseElementBuilder;
    private final CharacterReader characterReader;
    private final Evaluator evaluator;
    private final Environment environment;
    private final ReplOutput replOutput;

    private boolean finishedForm;

    // For now it's convenient to ensure we've got a singleton instance and easily acquired, e.g. by load() function.
    public static BatchEvaluator INSTANCE;

    public BatchEvaluator(SyntaxTreeBuilder syntaxTreeBuilder,
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
            finishedForm = true;
            endExpression(syntaxTreeBuilder, environment, evaluator);
        } else {
            // we're in the middle of a form
            if(c != '\n') {
                finishedForm = false;  // reset flag; only needs doing once per form really
            }
            if (!finishedForm && c == '\n') {
                replOutput.promptForMidForm();
            }
        }
    }

    private void endExpression(SyntaxTreeBuilder syntaxTreeBuilder,
                               Environment environment,
                               Evaluator evaluator) {
        RList topLevelList = syntaxTreeBuilder.getResult();
        parseElementBuilder.reset();
        Value<?> value = evaluator.evaluate(topLevelList, environment);
        replOutput.emitOutput(value.toString());
        replOutput.promptForNewForm();
    }
}
