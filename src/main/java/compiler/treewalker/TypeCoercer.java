package compiler.treewalker;

import syntaxtree.Atom;

public class TypeCoercer {
    public static TypedAtom<?> coerceType(Atom atom) {
        String stringValue = atom.value();

        try {
            int val = Integer.parseInt(stringValue);
            return new IntAtom(atom, val);
        }
        catch(NumberFormatException e) {}

        try {
            double val = Double.parseDouble(stringValue);
            return new FloatAtom(atom, val);
        }
        catch(NumberFormatException e) {}

        char firstChar = stringValue.charAt(0);
        if (firstChar == '"') {
            String stringValueNoQuotes = stringValue.replace("\"", "");
            return new StringAtom(atom, stringValueNoQuotes);
        } else if (firstChar == '#') {
            // only known case now is character literal (we've already handled other things like function quote in the Tokeniser
            if ('\\' == stringValue.charAt(1)) {
                // We can't super-easily parse the character due to special char values like #\Newline so we dispatch to something else
                return CharAtom.parse(atom);
            } else if (':' == stringValue.charAt(1)) {
                // gensym symbol
                return new SymbolAtom(atom, stringValue);
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
