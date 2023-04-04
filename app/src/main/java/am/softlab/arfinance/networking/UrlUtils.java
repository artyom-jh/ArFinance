package am.softlab.arfinance.networking;

import android.webkit.MimeTypeMap;

import java.net.IDN;
import java.nio.charset.Charset;

public class UrlUtils {

    /**
     * Convert IDN names to punycode if necessary
     */
    public static String convertUrlToPunycodeIfNeeded(String url) {
        if (!Charset.forName("US-ASCII").newEncoder().canEncode(url)) {
            if (url.toLowerCase().startsWith("http://")) {
                url = "http://" + IDN.toASCII(url.substring(7));
            } else if (url.toLowerCase().startsWith("https://")) {
                url = "https://" + IDN.toASCII(url.substring(8));
            } else {
                url = IDN.toASCII(url);
            }
        }
        return url;
    }

    /**
     * see http://stackoverflow.com/a/8591230/1673548
     */
    public static String getUrlMimeType(final String urlString) {
        if (urlString == null) {
            return null;
        }

        String extension = MimeTypeMap.getFileExtensionFromUrl(urlString);
        if (extension == null) {
            return null;
        }

        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String mimeType = mime.getMimeTypeFromExtension(extension);
        if (mimeType == null) {
            return null;
        }

        return mimeType;
    }
}
