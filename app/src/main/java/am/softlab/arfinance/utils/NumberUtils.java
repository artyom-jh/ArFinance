package am.softlab.arfinance.utils;

import java.text.DecimalFormat;

public class NumberUtils {
    private NumberUtils() {
        throw new AssertionError();
    }

    public static String formatDouble(double number){
        DecimalFormat formatterDecimal = new DecimalFormat("#,###,##0.00");
        return formatterDecimal.format(number);
    }

    public static String formatInteger(int number){
        DecimalFormat formatterDecimal = new DecimalFormat("#,###,##0");
        return formatterDecimal.format(number);
    }
}
