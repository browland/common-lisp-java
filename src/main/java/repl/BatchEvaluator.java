package repl;

import evaluator.Evaluator;
import evaluator.Value;
import reader.CharacterReader;
import syntaxtree.RList;
import syntaxtree.SyntaxTreeBuilder;

import java.util.Map;

public class BatchEvaluator {
    private final SyntaxTreeBuilder syntaxTreeBuilder;
    private final CharacterReader characterReader;
    private final Evaluator evaluator;
    private final Map<String, Value<?>> environment;
    private final ReplOutput replOutput;

    private boolean finishedForm;

    public BatchEvaluator(SyntaxTreeBuilder syntaxTreeBuilder,
                          CharacterReader characterReader,
                          Evaluator evaluator,
                          Map<String, Value<?>> environment,
                          ReplOutput replOutput) {
        this.syntaxTreeBuilder = syntaxTreeBuilder;
        this.characterReader = characterReader;
        this.evaluator = evaluator;
        this.environment = environment;
        this.replOutput = replOutput;
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
                               Map<String, Value<?>> environment,
                               Evaluator evaluator) {
        RList topLevelList = syntaxTreeBuilder.getResult();
        syntaxTreeBuilder.reset();
        Value<?> value = evaluator.evaluate(topLevelList, environment);
        replOutput.emitOutput(value.toString());
        replOutput.promptForNewForm();
    }
}
