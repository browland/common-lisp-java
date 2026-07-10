package treewalker;

import syntaxtree.Atom;

public final class CharAtom extends TypedAtom<Character> {
    public CharAtom(Atom atom, Character value) {
        super(atom, value);
    }

    static CharAtom parse(Atom atom) {
        String stringValue = atom.value();
        if (! stringValue.startsWith("#\\")) {
            throw new IllegalArgumentException("Can't parse character atom from " + stringValue);
        }

        if (stringValue.length() == 3) {
            // simple character
            char c = stringValue.charAt(2);
            return new CharAtom(atom, c);
        }

        // TODO Otherwise some named character e.g. #\Newline etc
        throw new IllegalArgumentException("Char literal not handled yet: " + stringValue);
    }
}
