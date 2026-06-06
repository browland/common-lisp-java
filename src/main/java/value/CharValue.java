package value;

public class CharValue extends Value<Character> {
    public CharValue(char c) {
        super(c, ValueType.CHARACTER);
    }

    @Override
    public String toString() {
        if(value == '\n') {
            return "#\\Newline";
        }
        return "#\\" + value;
    }
}
