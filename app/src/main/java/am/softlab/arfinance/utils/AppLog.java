package am.softlab.arfinance.utils;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;

import am.softlab.arfinance.BuildConfig;

/**
 * simple wrapper for Android log calls, enables recording & displaying log
 */
public class AppLog {
    public static final String TAG = "ArFinance";
    public static final int HEADER_LINE_COUNT = 2;

    private static boolean mEnableRecording = false;
    private static final int MAX_ENTRIES = 99;
    private static LogEntryList mLogEntries = new LogEntryList();

    private AppLog() {
        throw new AssertionError();
    }

    /*
     * defaults to false, pass true to capture log so it can be displayed by AppLogViewerActivity
     */
    public static void enableRecording(boolean enable) {
        mEnableRecording = enable;
    }

    public static void v(String tag, String message) {
        message = StringUtils.notNullStr(message);

        if (BuildConfig.DEBUG)
            Log.v(TAG + "-" + tag, message);

        addEntry(tag, LogLevel.v, message);
    }

    public static void d(String tag, String message) {
        message = StringUtils.notNullStr(message);

        if (BuildConfig.DEBUG)
            Log.d(TAG + "-" + tag, message);

        addEntry(tag, LogLevel.d, message);
    }

    public static void i(String tag, String message) {
        message = StringUtils.notNullStr(message);

        if (BuildConfig.DEBUG)
            Log.i(TAG + "-" + tag, message);

        addEntry(tag, LogLevel.i, message);
    }

    public static void w(String tag, String message) {
        message = StringUtils.notNullStr(message);

        if (BuildConfig.DEBUG)
            Log.w(TAG + "-" + tag, message);

        addEntry(tag, LogLevel.w, message);
    }

    public static void e(String tag, String message) {
        message = StringUtils.notNullStr(message);

        if (BuildConfig.DEBUG)
            Log.e(TAG + "-" + tag, message);

        addEntry(tag, LogLevel.e, message);
    }

    public static void e(String tag, String message, Throwable tr) {
        message = StringUtils.notNullStr(message);

        if (BuildConfig.DEBUG)
            Log.e(TAG + "-" + tag, message, tr);

        addEntry(tag, LogLevel.e, message + " - exception: " + tr.getMessage());
        addEntry(tag, LogLevel.e, "StackTrace: " + getStringStackTrace(tr));
    }

    public static void e(String tag, Throwable tr) {
        if (BuildConfig.DEBUG)
            Log.e(TAG + "-" + tag, tr.getMessage(), tr);

        addEntry(tag, LogLevel.e, tr.getMessage());
        addEntry(tag, LogLevel.e, "StackTrace: " + getStringStackTrace(tr));
    }

    public static void e(String tag, String volleyErrorMsg, int statusCode) {
        if (TextUtils.isEmpty(volleyErrorMsg)) {
            return;
        }
        String logText;
        if (statusCode == -1) {
            logText = volleyErrorMsg;
        } else {
            logText = volleyErrorMsg + ", status " + statusCode;
        }

        if (BuildConfig.DEBUG)
            Log.e(TAG + "-" + tag, logText);

        addEntry(tag, LogLevel.w, logText);
    }


    // ---------------------------------------------------------------------------------------------
    private enum LogLevel {
        v, d, i, w, e;
        private String toHtmlColor() {
            switch(this) {
                case v:
                    return "grey";
                case i:
                    return "black";
                case w:
                    return "purple";
                case e:
                    return "red";
                case d:
                default:
                    return "teal";
            }
        }
    }


    private static class LogEntryList extends ArrayList<LogEntry> {
        private synchronized boolean addEntry(LogEntry entry) {
            if (size() >= MAX_ENTRIES)
                removeFirstEntry();
            return add(entry);
        }
        private void removeFirstEntry() {
            Iterator<LogEntry> it = iterator();
            if (!it.hasNext())
                return;
            try {
                remove(it.next());
            } catch (NoSuchElementException e) {
                //noop
            }
        }
    }

    private static void addEntry(String tag, LogLevel level, String text) {
        // skip if recording is disabled (default)
        if (!mEnableRecording) {
            return;
        }

        LogEntry entry = new LogEntry(level, text, tag);
        mLogEntries.addEntry(entry);
    }


    private static String getStringStackTrace(Throwable throwable) {
        StringWriter errors = new StringWriter();
        throwable.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }


    /*
     * returns entire log as html for display (see AppLogViewerActivity)
     */
    public static ArrayList<String> toHtmlList(Context context) {
        ArrayList<String> items = new ArrayList<String>();

        // add version & device info - be sure to change HEADER_LINE_COUNT if additional lines are added
        items.add("<strong>ArFinance Android version: " + PackageUtils.getVersionName(context) + "</strong>");
        items.add("<strong>Android device name: " + DeviceUtils.getInstance().getDeviceName(context) + "</strong>");

        for (LogEntry mLogEntry : mLogEntries) {
            items.add(mLogEntry.toHtml());
        }

        return items;
    }


    /*
     * returns entire log as plain text
     */
    public static String toPlainText(Context context) {
        StringBuilder sb = new StringBuilder();

        // add version & device info
        sb.append("Rgs Android version: " + PackageUtils.getVersionName(context)).append("\n")
                .append("Android device name: " + DeviceUtils.getInstance().getDeviceName(context)).append("\n\n");

        Iterator<LogEntry> it = mLogEntries.iterator();
        int lineNum = 1;
        while (it.hasNext()) {
            sb.append(String.format("%02d - ", lineNum))
                    .append(it.next().mLogText)
                    .append("\n");
            lineNum++;
        }
        return sb.toString();
    }


    /* ----- class LogEntry  ----- */
    private static class LogEntry {
        LogLevel mLogLevel;
        String mLogText;
        String mLogTag;

        public LogEntry(LogLevel logLevel, String logText, String logTag) {
            mLogLevel = logLevel;
            mLogText = logText;

            if (mLogText == null) {
                mLogText = "null";
            }

            mLogTag = logTag;
        }

        private String toHtml() {
            StringBuilder sb = new StringBuilder();
            sb.append("<font color=\"");
            sb.append(mLogLevel.toHtmlColor());
            sb.append("\">");
            sb.append("[");
            sb.append(mLogTag);
            sb.append("] ");
            sb.append(mLogLevel.name());
            sb.append(": ");
            sb.append(TextUtils.htmlEncode(mLogText).replace("\n", "<br />"));
            sb.append("</font>");
            return sb.toString();
        }
    }
}
