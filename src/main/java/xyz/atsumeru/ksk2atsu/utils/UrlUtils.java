package xyz.atsumeru.ksk2atsu.utils;

/**
 * Small collection of Url utils
 */
public class UrlUtils {

    /**
     * Get hostname from given url. Example: <b>https://music.youtube.com/premium</b> -> <b>youtube</b>
     *
     * @param link url as {@link String}
     * @return url hostname
     */
    public static String getHostName(String link) {
        String host = getHost(link);
        if (host == null) {
            return "";
        } else {
            int levelTop = host.lastIndexOf(46);
            if (levelTop >= 0) {
                host = host.substring(0, levelTop);
            }

            if (host.contains(".")) {
                host = host.substring(host.lastIndexOf(".") + 1);
            }

            return host;
        }
    }

    /**
     * Get host from given url. Example: <b>https://music.youtube.com/premium</b> -> <b>music.youtube.com</b>
     *
     * @param link url as {@link String}
     * @return url host
     */
    public static String getHost(String link) {
        if (StringUtils.isNotEmpty(link)) {
            int scheme = link.indexOf("://");
            if (scheme < 0) {
                return null;
            } else {
                int start = scheme + 3;
                int end = link.indexOf(47, start);
                if (end < 0) {
                    end = link.length();
                }

                return link.substring(start, end);
            }
        }
        return null;
    }
}
