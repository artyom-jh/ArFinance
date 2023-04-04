package am.softlab.arfinance;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class DBManager {
    private static DBHelper mDBHelper;

    private final Context mContext;

    private static SQLiteDatabase mDatabase = null;

    public DBManager(Context context) {
        mContext = context;
    }

    public DBManager open() throws SQLException {
        if (mDBHelper == null)
            mDBHelper = DBHelper.getInstance(mContext);
        if (mDatabase == null)
            mDatabase = mDBHelper.getWritableDatabase();
        return this;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    public boolean isDatabaseOpened() {
        return mDatabase != null;
    }

    public void close() {
        if (mDBHelper != null)
            mDBHelper.close();
    }
}
