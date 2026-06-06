package value;

import java.io.InputStreamReader;

public class CharInputStreamValue extends Value<InputStreamReader> {
    public CharInputStreamValue(InputStreamReader isr) {
        super(isr, ValueType.CHAR_IN_STREAM);
    }
}
