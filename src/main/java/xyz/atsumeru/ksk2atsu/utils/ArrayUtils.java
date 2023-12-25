package xyz.atsumeru.ksk2atsu.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Small collection of {@link Collection} utils
 */
public class ArrayUtils {

    /**
     * Check is given {@link Collection} or array is empty or null
     *
     * @param collectionMapArray {@link Collection} or array to check
     * @return true if {@link Collection} or array is empty or null
     */
    public static boolean isEmpty(Object collectionMapArray) {
        int length = 1;
        if (collectionMapArray == null) {
            return true;
        } else if (collectionMapArray instanceof Collection) {
            return ((Collection) collectionMapArray).size() < length;
        } else if (collectionMapArray instanceof Map) {
            return ((Map) collectionMapArray).size() < length;
        } else if (!(collectionMapArray instanceof Object[])) {
            return true;
        } else {
            return ((Object[]) collectionMapArray).length < length || ((Object[]) collectionMapArray)[length - 1] == null;
        }
    }

    /**
     * Inversion of {@link #isEmpty(Object)} method
     *
     * @param collectionMapArray {@link Collection} or array to check
     * @return true if {@link Collection} or array is not empty
     */
    public static boolean isNotEmpty(Object collectionMapArray) {
        return !isEmpty(collectionMapArray);
    }

    /**
     * Splits given {@link String} into {@link List} with "," as delimiter
     *
     * @param str input {@link String}
     * @return {@link List} of {@link String}
     */
    public static List<String> splitString(String str) {
        if (StringUtils.isNotEmpty(str)) {
            return Arrays.stream(str.split(","))
                    .filter(StringUtils::isNotEmpty)
                    .map(String::trim)
                    .collect(Collectors.toList());
        }
        return null;
    }
}
