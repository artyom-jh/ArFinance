package am.softlab.arfinance;

import static am.softlab.arfinance.datasets.SettingsTable.CREATE_TABLE_SETTINGS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/*
 * !!! Important Note:
 * The SQLite database is lazily initialized. This means that it isn't actually created until
 * it's first accessed through a call to getReadableDatabase() or getWriteableDatabase().
 * This also means that any methods that call getReadableDatabase() or getWriteableDatabase() should
 * be done on a background thread as there is a possibility that they might be kicking off the
 * initial creation of the database.
 *
 * In any activity just pass the context and use the singleton method
 *    dbHelper = MyDBHelper.getInstance(this);
 */
public class DBHelper extends SQLiteOpenHelper {
    private static DBHelper mInstance;

    //Database Information
    private static final String DB_NAME = "ArFinanceDB";
    private static final int DB_VERSION = 1;


    public static synchronized DBHelper getInstance(Context context) {
        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (mInstance == null) {
            mInstance = new DBHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    /*
     * Constructor should be private to prevent direct instantiation.
     * Make a call to the static method "getInstance()" instead.
     */
    private DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }


    /*
     * Called when the database is created for the FIRST time.
     * If a database already exists on disk with the same DATABASE_NAME, this method will NOT be called.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_SETTINGS);
    }


    /*
     * Called when the database connection is being configured.
     * Configure database settings for things like foreign key support, write-ahead logging, etc.
     */
    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        //not used yet:  db.setForeignKeyConstraintsEnabled(true);
    }


    /*
     * Called when the database needs to be upgraded.
     * This method will only be called if a database already exists on disk with the same DATABASE_NAME,
     * but the DATABASE_VERSION is different than the version of the database that exists on disk.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            // Simplest implementation is to drop all old tables and recreate them
            //db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE);
            //onCreate(db);

            //todo
        }
    }

}
