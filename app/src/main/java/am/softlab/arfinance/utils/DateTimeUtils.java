package am.softlab.arfinance.utils;

import android.text.format.DateFormat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateTimeUtils {
    private DateTimeUtils() {
        throw new AssertionError();
    }

    /*
     * routines to return a diff between two dates - always return a positive number
     */
    public static long millisecondsBetween(Date dt1, Date dt2) {
        if (dt1==null || dt2==null)
            return 0;
        return Math.abs(dt1.getTime() - dt2.getTime());
    }
    public static int secondsBetween(Date dt1, Date dt2) {
        long msDiff = millisecondsBetween(dt1, dt2);
        if (msDiff == 0) {
            return 0;
        }
        return (int)(msDiff / 1000);
    }

    public static Date stringToDate(final String dateStr) {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        try {
            return format.parse(dateStr);
        } catch (ParseException e) {
            return null;
        }
    }

    //created a static method to convert timestamp to proper date format, so we can use
    //it everywhere in project, no need to rewrite again
    public static String formatTimestamp(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("dd/MM/yyyy", cal).toString();
    }

    public static String formatTimestampShort(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("dd/MM/yy", cal).toString();
    }

    public static String formatTimestampUnderline(long timestamp){
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timestamp);

        //formatting timestamp to dd/MM/yyyy
        return DateFormat.format("yyyy_MM_dd", cal).toString();
    }

}
