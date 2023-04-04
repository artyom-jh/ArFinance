package am.softlab.arfinance.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class EncryptionUtils {
    /*
     * Usage example
     * ------------------------------------------
     * To encrypt:
     *       EncUtil.generateKey("password");
     *       EncUtil.encryptMsg(String toEncrypt)
     * To decrypt:
     *       EncUtil.decryptMsg(byte[] toDecrypt)
     * ------------------------------------------
     */

    private static SecretKey mSecret = null;

    private EncryptionUtils() {
        throw new AssertionError();
    }

    public static void generateKey(String password)
            throws IllegalArgumentException
    {
        mSecret = new SecretKeySpec(password.getBytes(), "AES");
    }

    public static byte[] encryptMsg(String message)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalArgumentException, IllegalBlockSizeException, BadPaddingException
    {
        /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, mSecret);

        return cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
    }

    public static String decryptMsg(byte[] cipherText)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalArgumentException, BadPaddingException, IllegalBlockSizeException
    {
        /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, mSecret);

        return new String(cipher.doFinal(cipherText), StandardCharsets.UTF_8);
    }

    public static String base64Encode(byte[] bytesToEncode) {
        return Base64.encodeToString(bytesToEncode, Base64.NO_WRAP | Base64.URL_SAFE);
    }

    public static byte[] base64Decode(String bytesToDecode) {
        return Base64.decode(bytesToDecode, Base64.NO_WRAP | Base64.URL_SAFE);
    }

}
