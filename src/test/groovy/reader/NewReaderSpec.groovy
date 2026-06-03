//package reader
//
//import spock.lang.Specification
//
//class NewReaderSpec extends Specification {
//
//    def "basic block comment"() {
//        given:
//        def program = "#| testing |#";
//        NewReader reader = new NewReader();
//
//        when:
//        def result = reader.parse(program);
//
//        then:
//        result.size() == 1
//        def parseElement = result.getFirst() as NewReader.ParseElement
//        parseElement.matchedChars() == "#| testing |#"
//        parseElement.parseElementType() == NewReader.ParseElementType.BLOCK_COMMENT
//    }
//
//    def "multiple block comments at same level"() {
//        given:
//        def program = "#| testing 1 |#   #| testing 2 |#";
//        NewReader reader = new NewReader();
//
//        when:
//        def result = reader.parse(program);
//
//        then:
//        // We expect to only match the block comments as one group, and to not trigger the separate bare whitespace group.
//        result.size() == 1
//        def parseElement = result.getFirst() as NewReader.ParseElement
//        parseElement.matchedChars() == program
//        parseElement.parseElementType() == NewReader.ParseElementType.BLOCK_COMMENT
//    }
//
//    def "huge block comment does not stack overflow"() {
//        given:
//        def program = "#|" + " ".repeat(50000) + " |#";
//        NewReader reader = new NewReader();
//
//        when:
//        def result = reader.parse(program);
//
//        then:
//        // We expect to only match the block comments as one group, and to not trigger the separate bare whitespace group.
//        result.size() == 1
//        def parseElement = result.getFirst() as NewReader.ParseElement
//        parseElement.matchedChars() == program
//        parseElement.parseElementType() == NewReader.ParseElementType.BLOCK_COMMENT
//    }
//}
