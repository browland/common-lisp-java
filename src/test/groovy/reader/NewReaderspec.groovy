package reader

import spock.lang.Specification

class NewReaderspec extends Specification {

    def "basic block comment"() {
        given:
        def program = "#| testing |#";
        NewReader reader = new NewReader();

        when:
        def result = reader.parse(program);

        then:
        result.size() == 1
        def parseElement = result.getFirst() as NewReader.ParseElement
        parseElement.matchedChars() == "#| testing |#"
        parseElement.parseElementType() == NewReader.ParseElementType.BLOCK_COMMENT
    }

    def "multiple block comments at same level"() {
        given:
        def program = "#| testing 1 |#   #| testing 2 |#";
        NewReader reader = new NewReader();

        when:
        def result = reader.parse(program);

        then:
        result.size() == 1
        def parseElement = result.getFirst() as NewReader.ParseElement
        parseElement.matchedChars() == "#| testing 1 |#   #| testing 2 |#"
        parseElement.parseElementType() == NewReader.ParseElementType.BLOCK_COMMENT
    }
}
