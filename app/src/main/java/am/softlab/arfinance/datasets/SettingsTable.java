package am.softlab.arfinance.datasets;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

import am.softlab.arfinance.MyApplication;
import am.softlab.arfinance.models.ModelSettings;
import am.softlab.arfinance.utils.AppLog;

public class SettingsTable {
    public static final String SETTINGS_TABLE = "tbl_settings";

    // Post Table Columns
    private static final String KEY_ST_ID = "st_id";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_ST_ENC_PASS = "st_enc_pass";
    private static final String KEY_ST_MAX_PIE_SECTORS = "st_max_pie_sectors";

    public static final String CREATE_TABLE_SETTINGS =
            "CREATE TABLE IF NOT EXISTS " + SETTINGS_TABLE + " ("
                    + KEY_ST_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, "
                    + KEY_USER_ID + " TEXT NOT NULL UNIQUE, "
                    + KEY_ST_ENC_PASS + " TEXT DEFAULT '', "
                    + KEY_ST_MAX_PIE_SECTORS + " INTEGER NOT NULL DEFAULT 3);";

    private static final String TAG = "SETTINGS_TABLE_TAG";

    private SettingsTable() {
        throw new AssertionError();
    }

    /**
     * Insert or Update settings row for current user into the SETTINGS_TABLE
     * @param database - SQLiteDatabase database
     * @param model - ModelSettings object
     * @return ModelSettings - same model if updated, or new if inserted
     */
    public static ModelSettings addOrUpdateSetting(final SQLiteDatabase database, ModelSettings model) {
        // It's a good idea to wrap our insert in a transaction. This helps with performance and ensures
        // consistency of the database.
        database.beginTransaction();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_USER_ID, model.getUser_id());
            values.put(KEY_ST_ENC_PASS, model.getSt_enc_pass());
            values.put(KEY_ST_MAX_PIE_SECTORS, model.getSt_max_pie_sectors());

            // First try to update the user in case the user already exists in the database
            // This assumes userNames are unique
            int rows = database.update(SETTINGS_TABLE, values, KEY_USER_ID + "= ?", new String[] {""+model.getUser_id()} );

            // Check if update not succeeded -> add
            if (rows != 1) {
                long st_id = database.insertOrThrow(SETTINGS_TABLE, null, values);
                model.setSt_id(st_id);
                database.setTransactionSuccessful();
                AppLog.d(TAG, "Settings successfully inserted");
            }
            else
                AppLog.d(TAG, "Settings successfully updated");
        }
        catch (Exception e) {
            AppLog.e(TAG, "Error while trying to add settings to database");
        }
        finally {
            database.endTransaction();
        }

        return model;
    }

    @SuppressLint("Range")
    public static ModelSettings getUserSetting(SQLiteDatabase database, String uid) {
        String[] args = {uid};
        Cursor c = database.rawQuery("SELECT * FROM " + SETTINGS_TABLE + " WHERE " + KEY_USER_ID + "=?", args);

        ModelSettings model = new ModelSettings();
        try {
            if (c.moveToFirst()) {
                model.setSt_id(c.getLong(c.getColumnIndex(KEY_ST_ID)));
                model.setUser_id(c.getString(c.getColumnIndex(KEY_USER_ID)));
                model.setSt_enc_pass(c.getString(c.getColumnIndex(KEY_ST_ENC_PASS)));
                model.setSt_max_pie_sectors(c.getInt(c.getColumnIndex(KEY_ST_MAX_PIE_SECTORS)));
            }
        }
        finally {
            SqlUtils.closeCursor(c);
        }

        return model;
    }

    private static void dropTables(SQLiteDatabase db) {
        AppLog.i(TAG, "dropping settings table");
        db.execSQL("DROP TABLE IF EXISTS " + SETTINGS_TABLE);
    }

    public static void reset(SQLiteDatabase db) {
        AppLog.i(TAG, "resetting settings table");
        dropTables(db);
        db.execSQL(CREATE_TABLE_SETTINGS);
    }
}
