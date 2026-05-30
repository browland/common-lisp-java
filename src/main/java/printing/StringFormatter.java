package printing;

import exception.EvaluationException;
import value.Value;

import java.util.*;

public class StringFormatter {

    public static String format(String formatString, List<Value<?>> values) {
        List<String> formatStrings = extractFormatStrings(formatString);
        if (formatStrings.size() != values.size()) {
            throw new EvaluationException("Format strings length != parameters length in " + formatString + " with values " + values);
        }

        StringBuilder sb = new StringBuilder(formatString);
        int paramIndex = 0;
        for (String thisFormatString : formatStrings) {
            String formattedParameter = print(thisFormatString, values.get(paramIndex));
            int start = sb.indexOf(thisFormatString);
            sb.replace(start, start+2, formattedParameter);
            paramIndex++;
        }

        return sb.toString();
    }

    private static List<String> extractFormatStrings(String string) {
        int nextIndex = string.indexOf("~");
        List<String> formatStrings = new ArrayList<>();
        while (nextIndex != -1) {
            String formatString = string.substring(nextIndex, nextIndex + 2);
            formatStrings.add(formatString);
            nextIndex = string.indexOf("~", nextIndex + 1);
        }

        return formatStrings;
    }

    private static String print(String formatString, Value<?> value) {
        if("~S".equals(formatString)) {
            return value.toString();
        }
        else {
            throw new EvaluationException("unrecognised format string " + formatString);
        }
    }
}
