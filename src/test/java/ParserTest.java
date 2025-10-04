import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ParserTest {
    @Test
    void testParseSimpleExpression() {
        String program = """
                (add 1 2)
                """;

        String[] expressions = Parser.extractExpressionsAtThisLevel(program);

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

        String[] expressions = Parser.extractExpressionsAtThisLevel(program);

        assertThat(expressions.length).isEqualTo(3);
        assertThat(expressions[0]).isEqualTo("add");
        assertThat(expressions[1]).isEqualTo("1");
        assertThat(expressions[2]).isEqualTo("(add 1 (add 1 2))");
    }
}
