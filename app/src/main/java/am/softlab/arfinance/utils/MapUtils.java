package am.softlab.arfinance.utils;

import android.util.Base64;

import java.util.Date;
import java.util.Map;

/**
 * wrappers for extracting values from a Map object
 */
public class MapUtils {
    private MapUtils() {
        throw new AssertionError();
    }

    public static final class MyEntry<K, V> implements Map.Entry<K, V> {
        private final K key;
        private V value;

        public MyEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value;
        }

        @Override
        public V setValue(V object) {
            V old = this.value;
            this.value = value;
            return old;
        }
    }


    /**
     * returns a String value for the passed key in the passed map
     * always returns "" instead of null
     */
    public static String getMapStr(final Map<?, ?> map, final String key) {
        if (map == null || key == null || !map.containsKey(key) || map.get(key) == null) {
            return "";
        }
        return map.get(key).toString();
    }


    /**
     * returns an int value for the passed key in the passed map
     * defaultValue is returned if key doesn't exist or isn't a number
     */
    public static int getMapInt(final Map<?, ?> map, final String key) {
        return getMapInt(map, key, 0);
    }
    public static int getMapInt(final Map<?, ?> map, final String key, int defaultValue) {
        try {
            return Integer.parseInt(getMapStr(map, key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /**
     * long version of above
     */
    public static long getMapLong(final Map<?, ?> map, final String key) {
        return getMapLong(map, key, 0);
    }
    public static long getMapLong(final Map<?, ?> map, final String key, long defaultValue) {
        try {
            return Long.parseLong(getMapStr(map, key));
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    /**
     * returns a date object from the passed key in the passed map
     * returns null if key doesn't exist or isn't a date
     */
    public static Date getMapDate(final Map<?, ?> map, final String key) {
        if (map==null || key==null || !map.containsKey(key))
            return null;
        try {
            //return (Date) map.get(key);
            return new Date( getMapLong(map, key) );
        } catch (ClassCastException e) {
            return null;
        }
    }


    /**
     * returns a boolean value from the passed key in the passed map
     * returns true unless key doesn't exist, or the value is "0" or "false"
     */
    public static boolean getMapBool(final Map<?, ?> map, final String key) {
        String value = getMapStr(map, key);
        if (value.isEmpty())
            return false;
        if (value.startsWith("0")) // handles "0" and "0.0"
            return false;
        if (value.equalsIgnoreCase("false"))
            return false;
        // all other values are assume to be true
        return true;
    }


    public static byte[] getMapBytes(final Map<?, ?> map, final String key) {
        if (map == null || key == null || !map.containsKey(key) || map.get(key) == null) {
            return null;
        }
        return Base64.decode(map.get(key).toString(), Base64.DEFAULT);
    }
}
