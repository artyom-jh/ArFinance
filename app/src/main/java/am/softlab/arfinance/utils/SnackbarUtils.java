package am.softlab.arfinance.utils;

import android.content.Context;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;

public class SnackbarUtils {
    public enum Duration {SHORT, LONG}

    private SnackbarUtils() {
        throw new AssertionError();
    }


    public static Snackbar showSnackbar(View view, int stringResId) {
        return showSnackbar(view, stringResId, Duration.SHORT);
    }
    public static Snackbar showSnackbar(View view, int stringResId, SnackbarUtils.Duration duration) {
        Snackbar snackbar = Snackbar.make(view, stringResId,
                (duration == SnackbarUtils.Duration.SHORT ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG));
        snackbar.show();
        return snackbar;
    }

    public static Snackbar showSnackbar(View view, String text, SnackbarUtils.Duration duration) {
        Snackbar snackbar = Snackbar.make(view, text,
                (duration == SnackbarUtils.Duration.SHORT ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG));
        snackbar.show();
        return snackbar;
    }


    public static Snackbar showSnackbar(Context context, View view, int stringResId, SnackbarUtils.Duration duration) {
        return showSnackbar(context, view, context.getString(stringResId), duration);
    }
    public static Snackbar showSnackbar(Context context, View view, String text, SnackbarUtils.Duration duration) {
        Snackbar snackbar = Snackbar.make(context, view, text,
                (duration == SnackbarUtils.Duration.SHORT ? Snackbar.LENGTH_SHORT : Snackbar.LENGTH_LONG));
        snackbar.show();
        return snackbar;
    }
}
