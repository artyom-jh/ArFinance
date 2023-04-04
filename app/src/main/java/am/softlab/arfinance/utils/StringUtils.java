package am.softlab.arfinance.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

    private StringUtils() {
        throw new AssertionError();
    }

    /**
     * returns empty string if passed string is null, otherwise returns passed string
     */
    public static String notNullStr(String s) {
        if (s == null) {
            return "";
        }
        return s;
    }

    /**
     * returns true if two strings are equal or two strings are null
     */
    public static boolean equals(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        }
        return s1.equals(s2);
    }


    /* ----- stringToInt ----- */
    /**
     * simple wrapper for Integer.valueOf(string) so caller doesn't need to catch NumberFormatException
     */
    public static int stringToInt(String s) {
        return stringToInt(s, 0);
    }

    /**
     * simple wrapper for Integer.valueOf(string) with default value, so caller doesn't need to catch NumberFormatException
     */
    public static int stringToInt(String s, int defaultValue) {
        if (s == null)
            return defaultValue;
        try {
            return Integer.valueOf(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /* ----- stringToLong ----- */
    /**
     * simple wrapper for Long.valueOf(string) so caller doesn't need to catch NumberFormatException
     */
    public static long stringToLong(String s) {
        return stringToLong(s, 0L);
    }

    /**
     * simple wrapper for Long.valueOf(string) with default value, so caller doesn't need to catch NumberFormatException
     */
    public static long stringToLong(String s, long defaultValue) {
        if (s == null)
            return defaultValue;
        try {
            return Long.valueOf(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


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

        NumberFormat nf = NumberFormat.getInstance();
        char decSeparator = '.';
        if (nf instanceof DecimalFormat) {
            DecimalFormatSymbols sym = ((DecimalFormat) nf).getDecimalFormatSymbols();
            decSeparator = sym.getDecimalSeparator();
        }

        retval = retval.replaceAll("[^\\d" + decSeparator + "]", "");
        retval = retval.replace(',', '.');

        return retval;
    }
}
