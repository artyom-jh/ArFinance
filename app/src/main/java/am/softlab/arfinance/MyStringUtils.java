package am.softlab.arfinance;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MyStringUtils {

    public static String removeDateTimeFromString(String string) {
        // \\s+     - space
        // \b       - a word break and will work for both spaces and end of lines
        // (^|\s+)  - match space or start of string
        // ($|\s+)  - match space or end of string

        String retval = string;
        boolean matchFound;

        // if first entry is surrounded with spaces from two sides, then the closest next date is not found,
        // so we put it in do while cycle to make many cycles
        do {
            //looking for a pattern DD/MM/YYYY or then looking for a DD/MM
            Matcher matcher = Pattern.compile("((^|\\s+)\\d{1,2}/\\d{1,2}/\\d{4}($|\\s+)|(^|\\s+)\\d{2}/\\d{2}($|\\s+))", Pattern.CASE_INSENSITIVE).matcher(retval);
            matchFound = matcher.find();
            retval = matcher.replaceAll(" ");

            //looking for a pattern DD.MM.YYYY
            matcher = Pattern.compile("((^|\\s+)\\d{1,2}.\\d{1,2}.\\d{4}($|\\s+))", Pattern.CASE_INSENSITIVE).matcher(retval);
            matchFound = matchFound || matcher.find();
            retval = matcher.replaceAll(" ");

            //looking for a pattern hh:mm:ss or then looking for a hh:mm
            matcher = Pattern.compile("((^|\\s+)\\d{2}:\\d{2}:\\d{2}($|\\s+)|\\s+\\d{2}:\\d{2}($|\\s+))", Pattern.CASE_INSENSITIVE).matcher(retval);
            matchFound = matchFound || matcher.find();
            retval = matcher.replaceAll(" ");
        } while (matchFound);

        return retval;
    }

    public static String cleanAllExceptDigitsAndDecimal(String string) {
        String retval = string;

        retval = retval.replaceAll("[^\\d.]", "");

        return retval;
    }
}
