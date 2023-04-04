package am.softlab.arfinance.utils;

import android.content.Context;
import android.os.Build;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DeviceUtils {
    private static DeviceUtils instance;

    private static final String TAG = "DEVICE_UTILS_TAG";

    public static DeviceUtils getInstance() {
        if (instance == null) {
            instance = new DeviceUtils();
        }
        return instance;
    }

    public String getDeviceName(Context context) {
        String manufacturer = Build.MANUFACTURER;
        String undecodedModel = Build.MODEL;
        String model = null;

        try {
            Properties prop = new Properties();
            InputStream fileStream;

            // Read the device name from a precomplied list:
            // see http://making.meetup.com/post/29648976176/human-readble-android-device-names
            fileStream = context.getAssets().open("android_models.properties");
            prop.load(fileStream);
            fileStream.close();

            String decodedModel = prop.getProperty(undecodedModel.replaceAll(" ", "_"));
            if (decodedModel != null && !decodedModel.trim().equals("")) {
                model = decodedModel;
            }
        }
        catch (IOException e) {
            AppLog.e(TAG, e.getMessage());
        }

        if (model == null) {  //Device model not found in the list
            if (undecodedModel.startsWith(manufacturer)) {
                model = capitalize(undecodedModel);
            }
            else {
                model = capitalize(manufacturer) + " " + undecodedModel;
            }
        }
        return model;
    }

    private String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }

        char first = s.charAt(0);

        if (Character.isUpperCase(first)) {
            return s;
        }
        else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }
}
