package am.softlab.arfinance;

import java.util.ArrayList;
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
}
