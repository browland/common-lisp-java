package reader;

public record CharacterReaderEvent(char character,
                                   CharacterType type,
                                   int depth) {
}
