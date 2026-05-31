package reader

import spock.lang.Specification

class NewReaderspec extends Specification {

    def "basic block comment"() {
        given:
        def program = "#| testing |#";
        NewReader reader = new NewReader();

        when:
        def result = reader.nextParseElement(program);

        then:
        result.isPresent()
        def parseElement = result.get() as NewReader.ParseElement
        parseElement.matchedChars() == "#| testing |#"
        parseElement.parseElementType() == NewReader.ParseElementType.BLOCK_COMMENT
    }
}
