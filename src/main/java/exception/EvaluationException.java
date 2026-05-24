package exception;

import syntaxtree.Node;

public class EvaluationException extends RuntimeException {
    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(String message, Node form) {
        super(message + " while evaluating " + form);
    }
}
