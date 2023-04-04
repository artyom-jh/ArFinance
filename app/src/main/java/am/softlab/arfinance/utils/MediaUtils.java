package am.softlab.arfinance.utils;

import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import am.softlab.arfinance.networking.UrlUtils;

public class MediaUtils {
    private static final String TAG = "MEDIA_UTILS_TAG";

    private MediaUtils() {
        throw new AssertionError();
    }

    public static boolean isValidImage(String url) {
        if (url == null) {
            return false;
        }
        return url.endsWith(".png") || url.endsWith(".jpg") || url.endsWith(".jpeg") || url.endsWith(".rgjpg") || url.endsWith(".gif");
    }

    public static String getExtensionForMimeType(String mimeType) {
        if (TextUtils.isEmpty(mimeType))
            return "";

        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        String fileExtensionFromMimeType = mimeTypeMap.getExtensionFromMimeType(mimeType);
        if (TextUtils.isEmpty(fileExtensionFromMimeType)) {
            // We're still without an extension - split the mime type and retrieve it
            String[] split = mimeType.split("/");
            fileExtensionFromMimeType = split.length > 1 ? split[1] : split[0];
        }

        return fileExtensionFromMimeType.toLowerCase();
    }

    public static String getMediaFileMimeType(File mediaFile) {
        String originalFileName = mediaFile.getName().toLowerCase();
        String mimeType = UrlUtils.getUrlMimeType(originalFileName);

        if (TextUtils.isEmpty(mimeType)) {
            try {
                String filePathForGuessingMime;
                if (mediaFile.getPath().contains("://")) {
                    filePathForGuessingMime = Uri.encode(mediaFile.getPath(), ":/");
                } else {
                    filePathForGuessingMime = "file://"+ Uri.encode(mediaFile.getPath(), "/");
                }
                URL urlForGuessingMime = new URL(filePathForGuessingMime);
                URLConnection uc = urlForGuessingMime.openConnection();
                String guessedContentType = uc.getContentType(); //internally calls guessContentTypeFromName(url.getFile()); and guessContentTypeFromStream(is);
                // check if returned "content/unknown"
                if (!TextUtils.isEmpty(guessedContentType) && !guessedContentType.equals("content/unknown")) {
                    mimeType = guessedContentType;
                }
            } catch (MalformedURLException e) {
                AppLog.e(TAG, "MalformedURLException while trying to guess the content type for the file here " + mediaFile.getPath() + " with URLConnection", e);
            }
            catch (IOException e) {
                AppLog.e(TAG, "Error while trying to guess the content type for the file here " + mediaFile.getPath() +" with URLConnection", e);
            }
        }

        // No mimeType yet? Try to decode the image and get the mimeType from there
        if (TextUtils.isEmpty(mimeType)) {
            try {
                DataInputStream inputStream = new DataInputStream(new FileInputStream(mediaFile));
                String mimeTypeFromStream = getMimeTypeOfInputStream(inputStream);
                if (!TextUtils.isEmpty(mimeTypeFromStream)) {
                    mimeType = mimeTypeFromStream;
                }
                inputStream.close();
            } catch (FileNotFoundException e) {
                AppLog.e(TAG, "FileNotFoundException while trying to guess the content type for the file " + mediaFile.getPath(), e);
            } catch (IOException e) {
                AppLog.e(TAG, "IOException while trying to guess the content type for the file " + mediaFile.getPath(), e);
            }
        }

        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "";
        } else {
            if (mimeType.equalsIgnoreCase("video/mp4v-es")) { //Fixes #533. See: http://tools.ietf.org/html/rfc3016
                mimeType = "video/mp4";
            }
        }

        return mimeType;
    }

    public static String getMediaFileName(File mediaFile, String mimeType) {
        String originalFileName = mediaFile.getName().toLowerCase();
        String extension = MimeTypeMap.getFileExtensionFromUrl(originalFileName);
        if (!TextUtils.isEmpty(extension))  //File name already has the extension in it
            return originalFileName;

        if (!TextUtils.isEmpty(mimeType)) { //try to get the extension from mimeType
            String fileExtension = getExtensionForMimeType(mimeType);
            if (!TextUtils.isEmpty(fileExtension)) {
                originalFileName += "." + fileExtension;
            }
        } else {
            //No mimetype and no extension!!
            AppLog.e(TAG, "No mimetype and no extension for " + mediaFile.getPath());
        }

        return originalFileName;
    }

    public static String getMimeTypeOfInputStream(InputStream stream) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(stream, null, options);
        return options.outMimeType;
    }
}
