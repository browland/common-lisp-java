import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {
    // TODO need higher level thing to extract top-level forms.  Each of these would be passed in to the existing splitEx....

    @Test
    void testParseSimpleExpression() {
        String program = """
                (add 1 2)
                """;

        String[] expressions = Parser.splitExpressionsAtThisLevel(program);

        assertThat(expressions.length).isEqualTo(3);
        assertThat(expressions[0]).isEqualTo("add");
        assertThat(expressions[1]).isEqualTo("1");
        assertThat(expressions[2]).isEqualTo("2");
    }

    @Test
    void testParseWithSubExpression() {
        String program = """
                (add 1 (add 1 (add 1 2)))
                """;

        String[] expressions = Parser.splitExpressionsAtThisLevel(program);

        assertThat(expressions.length).isEqualTo(3);
        assertThat(expressions[0]).isEqualTo("add");
        assertThat(expressions[1]).isEqualTo("1");
        assertThat(expressions[2]).isEqualTo("(add 1 (add 1 2))");
    }

    @Test
    void testParseLetExpression() {
        String program = """
                (let ((x 1) (y 2)) (add x y))
                """;

        String[] expressions = Parser.splitExpressionsAtThisLevel(program);

        assertThat(expressions.length).isEqualTo(3);
        assertThat(expressions[0]).isEqualTo("let");
        assertThat(expressions[1]).isEqualTo("((x 1) (y 2))");
        assertThat(expressions[2]).isEqualTo("(add x y)");

        String[] bindExpressions = Parser.splitExpressionsAtThisLevel(expressions[1]);
        assertThat(bindExpressions.length).isEqualTo(2);
        assertThat(bindExpressions[0]).isEqualTo("(x 1)");
        assertThat(bindExpressions[1]).isEqualTo("(y 2)");

        String[] binding1Separated = Parser.splitExpressionsAtThisLevel(bindExpressions[0]);
        assertThat(binding1Separated.length).isEqualTo(2);
        assertThat(binding1Separated[0]).isEqualTo("x");
        assertThat(binding1Separated[1]).isEqualTo("1");
    }
}
