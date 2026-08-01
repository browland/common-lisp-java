package compiler.treewalker;

import syntaxtree.Atom;

public class TypeCoercer {
    static TypedAtom<?> coerceType(Atom atom) {
        String stringValue = atom.value();
        char firstChar = stringValue.charAt(0);
        if (firstChar == '"') {
            String stringValueNoQuotes = stringValue.replace("\"", "");
            return new StringAtom(atom, stringValueNoQuotes);
        } else if (Character.isDigit(firstChar)) {
            // must be either int or float (symbols can't start with a digit)
            for (int j = 1; j < stringValue.length(); j++) {  // inner loop to detect decimal point
                char innerC = stringValue.charAt(j);
                if (innerC == '.') {
                    Double d = Double.parseDouble(stringValue);
                    return new FloatAtom(atom, d);
                }
            }
            // at this point it must be an integer type
            Integer intValue = Integer.parseInt(stringValue);
            return new IntAtom(atom, intValue);
        } else if (firstChar == '#') {
            // only known case now is character literal (we've already handled other things like function quote in the Tokeniser
            if ('\\' == stringValue.charAt(1)) {
                // We can't super-easily parse the character due to special char values like #\Newline so we dispatch to something else
                return CharAtom.parse(atom);
            } else {
                throw new IllegalArgumentException("Unhandled atom for type co-ercing: " + atom);
            }
        } else {
            // otherwise symbol and nothing else??
            // Not interning the symbol here as this is still quite raw; likely to do that before using the symbol in any environment for example
            return new SymbolAtom(atom, stringValue);
        }
    }
}
