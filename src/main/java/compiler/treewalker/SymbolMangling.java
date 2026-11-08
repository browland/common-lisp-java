package compiler.treewalker;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

/**
 * Reformat symbols to a hex scheme so they can be written as asm labels without using any reserved chars.
 */
public class SymbolMangling {

    public static String mangle(String symbol) {
        if (!needsMangling(symbol)) {
            return symbol;
        }
        return "CL_" + HexFormat.of().withUpperCase()
                .formatHex(symbol.getBytes(StandardCharsets.UTF_8));
    }

    public static boolean needsMangling(String symbol) {
        return symbol.contains(":") || symbol.contains("+");
    }

//    public static String unmangle(String mangledSymbol) {
//        if (! mangledSymbol.startsWith("CL_")) {
//            throw new IllegalArgumentException("Not mangled symbol, should begin with CL_");
//        }
//
//        byte[] bytes = HexFormat.of().parseHex(mangledSymbol.substring(3));
//        return new String(bytes, StandardCharsets.UTF_8);
//    }
}
