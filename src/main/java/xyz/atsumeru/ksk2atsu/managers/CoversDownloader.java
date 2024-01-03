package xyz.atsumeru.ksk2atsu.managers;

import me.tongfei.progressbar.ProgressBar;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.utils.ComicUtils;
import xyz.atsumeru.ksk2atsu.utils.FileUtils;
import xyz.atsumeru.ksk2atsu.utils.ProgressBarBuilder;
import xyz.atsumeru.ksk2atsu.utils.StringUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CoversDownloader {
    private static final String MAGAZINE_URL = "https://www.fakku.net/magazines/%s-%s";
    private static final String COVER_TAG = "object-cover\" src=\"";
    private static final Map<String, String> PREDEFINED_IMAGE_URLS_MAP = new HashMap<>() {{
        put("comic-bavel-2015-02", "https://i.imgur.com/FUJU8Um.jpg");
        put("comic-bavel-2015-04", "https://i.imgur.com/emXvimh.jpg");
        put("comic-bavel-2015-06", "https://i.imgur.com/lFhJk6p.jpg");
        put("comic-bavel-2015-08", "https://i.imgur.com/gXL8zAl.jpg");
        put("comic-bavel-2015-09", "https://i.imgur.com/OsItP41.jpg");
        put("comic-bavel-2015-10", "https://i.imgur.com/PX1ZuVC.jpg");
        put("comic-bavel-2015-11", "https://i.imgur.com/UpxVrGx.jpg");
        put("comic-bavel-2015-12", "https://i.imgur.com/HQI9SQg.jpg");
        put("comic-bavel-2016-10", "https://i.imgur.com/BGYgEI6.jpg");
        put("comic-bavel-2016-11", "https://i.imgur.com/PZBycG5.jpg");
        put("comic-bavel-2016-12", "https://i.imgur.com/JnvJZ7L.jpg");
        put("comic-europa-vol02", "https://i.imgur.com/X7p59s6.jpg");
        put("comic-europa-vol03", "https://i.imgur.com/CemBTN2.jpg");
        put("comic-europa-vol07", "https://i.imgur.com/xGtjsKS.jpg");
        put("comic-kairakuten-2016-01", "https://i.imgur.com/21e3S9f.jpg");
    }};

    private static ProgressBar progressBar;

    /**
     * Download covers for Comic Magazines in provided {@link File} directory
     *
     * @param inputDir input {@link File} directory
     * @return {@link List} of {@link String} download errors
     */
    public static List<String> download(File inputDir) {
        // Count Series in directory
        progressBar = ProgressBarBuilder.create(
                "Downloading covers:",
                (int) FileUtils.listDirs(inputDir)
                        .parallelStream()
                        .map(Path::toFile)
                        .map(FileUtils::listDirs)
                        .mapToLong(Collection::size)
                        .sum()
        );

        // List all Series in directory and download covers
        List<String> list = FileUtils.listDirs(inputDir)
                .parallelStream()
                .map(Path::toFile)
                .map(FileUtils::listDirs)
                .flatMap(Collection::stream)
                .filter(dir -> !findAndDownloadCover(dir))
                .map(Path::toString)
                .collect(Collectors.toList());

        progressBar.close();

        return list;
    }

    /**
     * Check if cover already exists, create magazine url, parser cover image and download it
     *
     * @param dir input {@link File} directory
     * @return true if cover already downloaded or download was successful
     */
    private static boolean findAndDownloadCover(Path dir) {
        progressBar.step();
        File coverFile = new File(dir.toFile(), "cover.jpg");

        // Check if cover already exists or parser and download it
        return coverFile.exists() || downloadImage(parseImageUrl(createMagazineUrl(dir)), coverFile);
    }

    /**
     * Parse folder name, detect Magazine and Isuue and construct magazine url
     *
     * @param dir input {@link File} directory
     * @return magazine {@link String} url
     */
    private static String createMagazineUrl(Path dir) {
        // Parse Comic Magazine name and Issue from file names
        String comicName = dir.getParent().getFileName().toString().replace(" ", "-").replace("COMIC", "Comic");
        String comicIssue = dir.getFileName().toString().replaceAll(".* - ", "");

        // Remove square brackets
        if (comicIssue.contains("[")) {
            comicIssue = comicIssue.replaceAll(".*\\[(.*?)]", "$1");
        }

        // Fix some errors in Comic Issue name
        comicIssue = comicIssue.replace(" ", "-")
                .replace("Volume", "Vol")
                .replace("#", "");

        // Format magazine url and fix some known errors
        return ComicUtils.fixMagazineUrl(String.format(MAGAZINE_URL, comicName, comicIssue));
    }

    /**
     * Parser cover image urls from given magazine url
     *
     * @param magazineUrl magazine {@link String} url to parse
     * @return cover {@link String} url
     */
    private static String parseImageUrl(String magazineUrl) {
        // Check if predefined static image url already present
        String predefinedImageUrl = PREDEFINED_IMAGE_URLS_MAP.get(magazineUrl.replaceAll(".*/", ""));
        if (StringUtils.isNotEmpty(predefinedImageUrl)) {
            return predefinedImageUrl;
        }

        // Request magazine url
        Request request = new Request.Builder()
                .url(magazineUrl)
                .build();

        // Get HTML and parse cover url
        try (Response response = new OkHttpClient().newCall(request).execute()) {
            StringBuilder html = new StringBuilder(response.body().string());
            int indexStart = html.indexOf(COVER_TAG) + COVER_TAG.length();
            int indexEnd = html.indexOf("\"", indexStart);

            // Parse cover url, remove -thumb modifier and fix some known issues
            return ComicUtils.fixCoverUrl(html.substring(indexStart, indexEnd).replace("-thumb", ""));
        } catch (Exception e) {
            if (App.IS_DEBUG) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * Download image from given url and save it into given {@link File}
     *
     * @param imageUrl   cover {@link String} url
     * @param outputFile {@link File} in which downloaded file will be saved
     * @return true if download success
     */
    private static boolean downloadImage(String imageUrl, File outputFile) {
        if (!StringUtils.isNotEmpty(imageUrl)) {
            return false;
        }

        try {
            // Request image url
            Request request = new Request.Builder()
                    .url(imageUrl)
                    .build();

            // Download image if response code is HTTP OK
            try (Response response = new OkHttpClient().newCall(request).execute()) {
                if (response.code() == HttpURLConnection.HTTP_OK) {
                    // Open an output stream to save into file
                    FileOutputStream outputStream = new FileOutputStream(outputFile);
                    // Save stream into file
                    IOUtils.write(response.body().bytes(), outputStream);
                    // Close stream
                    outputStream.close();
                    return true;
                }
            } catch (Exception e) {
                if (App.IS_DEBUG) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            if (App.IS_DEBUG) {
                e.printStackTrace();
            }
        }
        return false;
    }
}
