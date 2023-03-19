package am.softlab.arfinance;

import java.util.Arrays;
import java.util.List;

public class Constants {

    public static final long MAX_BYTES_UPLOAD = 50000000;

    public static final int PAGE_INCOME = 1;
    public static final int PAGE_EXPENSES = 2;

    // DB Transaction Types - used in MyApplication.updateWalletBalance
    public static final int ROW_ADDED = 1;
    public static final int ROW_UPDATED = 2;
    public static final int ROW_DELETED = 3;

    public static final int PIE_MAX_ENTRIES = 6;

    public static final int CAMERA_PERMISSION_CODE = 100;
    public static final int WRITE_EXTERNAL_STORAGE = 101;
    public static final int POST_NOTIFICATIONS = 102;

    public static final String CHANNEL_ID = "ARFINANCE_CHANNEL";
    public static final String WORK_ID = "ARFINANCE_WORK";

    // ATTENTION - dont change elements order -
    //             index of array elements used in LoginActivity.initTablesAndStartDashboard !!!
    public static final List<List<String>> CURRENCY_ARRAY_LIST = Arrays.asList(
            Arrays.asList("AMD", "Armenian Dram", "֏"),
            Arrays.asList("AUD", "Australian Dollar", "A$"),
            Arrays.asList("GBP", "British Pound Sterling", "£"),
            Arrays.asList("CAD", "Canadian Dollar", "C$"),
            Arrays.asList("CNY", "Chinese Yuan", "¥"),
            Arrays.asList("EUR", "Euro", "€"),
            Arrays.asList("JPY", "Japanese Yen", "￥"),
            Arrays.asList("RUB", "Russian Ruble", "₽"),
            Arrays.asList("KRW", "South Korean Won", "₩"),
            Arrays.asList("CHF", "Swiss Franc", "₣"),
            Arrays.asList("USD", "US Dollar", "$")
    );

    public static final String[] categoryTypesArray = new String[] {
            "Salary",           //Basic salary
            "Scholarship",
            "Other Income",     // !!! Last Income

            "Household",
            "Healthcare",
            "Gifts",            //Gifts for friends and family
            "Vacation",
            "Education",        //Expenses for education - tuition fees, books, courses, etc.
            "Clothing",
            "Leisure",
            "Groceries",        //Food expenses
            "Phones and Internet",
            "Transport",
            "Entertainment"
    };
    public static final String[] categoryNotesArray = new String[] {
            "Basic salary",         //Salary
            "",                     //Scholarship
            "",                     //Other Income

            "",                     //Household
            "",                     //Healthcare
            "Gifts for friends and family",     //Gifts
            "",                     //Vacation
            "Expenses for education - tuition fees, books, courses, etc.",      //Education
            "",                     //Clothing
            "",                     //Leisure
            "Food expenses",        //Groceries
            "",                     //Phones and Internet
            "",                     //Transport
            ""                      //Entertainment
    };

    public static final long MILLIS_IN_DAY = 86400000L;
    public static final long MILLIS_IN_WEEK = 604800000L;
    public static final long MILLIS_IN_MONTH = 2629746000L;
    public static final long MILLIS_IN_YEAR = 31556952000L;
    public static final int PERIOD_UNKNOWN = -1;  // ATTENTION - see periodsArray
    public static final int PERIOD_DAILY = 0;     // ATTENTION - see periodsArray
    public static final int PERIOD_WEEKLY = 1;    // ATTENTION - see periodsArray
    public static final int PERIOD_MONTHLY = 2;   // ATTENTION - see periodsArray
    public static final int PERIOD_YEARLY = 3;    // ATTENTION - see periodsArray
    public static final String[] periodsArray = new String[] {
            "Unknown",
            "Daily",
            "Weekly",
            "Monthly",
            "Yearly"
    };
}
