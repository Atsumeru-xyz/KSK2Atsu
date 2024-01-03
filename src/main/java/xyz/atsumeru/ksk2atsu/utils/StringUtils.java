package xyz.atsumeru.ksk2atsu.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

/**
 * Small collection of {@link String} utils
 */
public class StringUtils {

    /**
     * Check is given {@link String} is not empty (contains at least one character)
     *
     * @param str {@link String} to check
     * @return true if {@link String} is not empty
     */
    public static boolean isNotEmpty(String str) {
        return str != null && str.trim().length() > 0;
    }

    /**
     * Case-insensitive check equality of two given {@link String}
     *
     * @param s1 first {@link String}
     * @param s2 second {@link String}
     * @return true if {@link String} equals
     */
    public static boolean equalsIgnoreCase(String s1, String s2) {
        return Optional.ofNullable(s1)
                .map(s -> s.equalsIgnoreCase(s2))
                .orElse(false);
    }

    /**
     * MD5 hash generating from given {@link String}
     *
     * @param str input {@link String}
     * @return MD5 hash from given {@link String}
     */
    public static String md5Hex(String str) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                final String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NullPointerException | NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        }
        return "";
    }
}
