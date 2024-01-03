package xyz.atsumeru.ksk2atsu.metadata;

import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;
import xyz.atsumeru.ksk2atsu.database.models.Content;
import xyz.atsumeru.ksk2atsu.utils.ArrayUtils;
import xyz.atsumeru.ksk2atsu.utils.ComicUtils;
import xyz.atsumeru.ksk2atsu.utils.StringUtils;
import xyz.atsumeru.ksk2atsu.utils.UrlUtils;
import xyz.atsumeru.ksk2atsu.zip.ZipIterator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Model for book_info.json metadata that will be saved into all archives in raw dump
 */
public class BookInfo {
    @Getter
    private String title;
    @Getter
    private String authors;
    @Getter
    private String event;
    @Getter
    private String link;

    /**
     * Save metadata into file in filesystem
     *
     * @param path        path to file in which metadata will be saved
     * @param content     {@link Content} with actual metadata that will be converted into json format
     * @param serieHash   special Atsumeru hash that represents Serie uniq identifier
     * @param contentHash special Atsumeru hash that represents Archive uniq identifier
     * @param isSerie     indicates if actual metadata is intended for Atsumeru Serie
     * @param isDoujinshi indicates that actual Atsumeru Archive is {@link CatalogType#DOUJIN} and depending on that,
     *                    some result metadata will change
     */
    public static void saveToFile(String path, Content content, String serieHash, String contentHash, boolean isSerie, boolean isDoujinshi) {
        try {
            Writer writerBookInfo = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8);
            writerBookInfo.write(toJSON(content, serieHash, contentHash, isSerie, isDoujinshi).toString(4));
            writerBookInfo.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Same method as {@link #saveToFile(String, Content, String, String, boolean, boolean)} but saves json metadata
     * directly into archive file
     *
     * @param archive     archive {@link File} into which metadata will be saved
     * @param content     {@link Content} with actual metadata that will be converted into json format
     * @param serieHash   special Atsumeru hash that represents Serie uniq identifier
     * @param contentHash special Atsumeru hash that represents Archive uniq identifier
     * @param isSerie     indicates if actual metadata is intended for Atsumeru Serie
     * @param isDoujinshi indicates that actual Atsumeru Archive is {@link CatalogType#DOUJIN} and depending on that,
     *                    some result metadata will change
     */
    public static void saveIntoArchive(File archive, Content content, String serieHash, String contentHash, boolean isSerie, boolean isDoujinshi) {
        try (ZipIterator zipIterator = ZipIterator.open(archive)) {
            Map<String, String> contentToSave = new HashMap<>();
            contentToSave.put(App.BOOK_INFO_JSON, toJSON(content, serieHash, contentHash, isSerie, isDoujinshi).toString(4));
            zipIterator.saveIntoArchive(archive.toString(), contentToSave);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Converts {@link Content} model into {@link JSONObject} that will be saved as metadata
     *
     * @param content     {@link Content} with actual metadata that will be converted into json format
     * @param serieHash   special Atsumeru hash that represents Serie uniq identifier
     * @param contentHash special Atsumeru hash that represents Archive uniq identifier
     * @param isSerie     indicates if actual metadata is intended for Atsumeru Serie
     * @param isDoujinshi indicates that actual Atsumeru Archive is {@link CatalogType#DOUJIN} and depending on that,
     *                    some result metadata will change
     * @return {@link JSONObject} with metadata
     */
    public static JSONObject toJSON(Content content, String serieHash, String contentHash, boolean isSerie, boolean isDoujinshi) {
        JSONObject obj = new JSONObject();

        // Basic Metadata
        putHashes(obj, serieHash, contentHash);

        putJSON(obj, "link", content.getUrl());
        putLinks(obj, content.getUrl());
        putJSON(obj, "cover", contentHash);

        // Titles
        putJSON(obj, "title", content.getTitle());
        putJSON(obj, "alt_title", content.getAuthor());

        // Main info
        putJSON(obj, "country", "Japan");
        putJSON(obj, "publisher", content.getPublisher());

        Optional.ofNullable(content.getMagazine())
                .map(magazine -> ComicUtils.detectComicNameAndIssue(magazine.split(",")[0].trim(), 0))
                .map(pair -> pair.second)
                .filter(issueYear -> issueYear.startsWith("20"))
                .ifPresent(issueYear -> putJSON(obj, "published", issueYear));

        putJSON(obj, "event", content.getEvent());
        putJSON(obj, "description", content.getDescription());

        // Info lists
        putJSON(obj, "authors", content.getAuthor());
        putJSON(obj, "artists", ArrayUtils.splitString(content.getArtists()));
        putJSON(obj, "languages", ArrayUtils.splitString(content.getLanguage()));
        putJSON(obj, "translators", ArrayUtils.splitString(content.getPublisher()));
        putJSON(obj, "parodies", ArrayUtils.splitString(content.getParodies()));
        putJSON(obj, "circles", ArrayUtils.splitString(content.getCircles()));
        putJSON(obj, "magazines", ArrayUtils.splitString(content.getMagazine()));

        // Genres/Tags
        putJSON(obj, "tags", content.getTags());

        // Age Rating
        putJSON(obj, "age_rating", "ADULTS_ONLY");

        // Statuses
        putJSON(obj, "status", !isSerie ? "COMPLETE" : "MAGAZINE");
        putJSON(obj, "translation_status", !isSerie ? "COMPLETE" : "ONGOING");
        putJSON(obj, "censorship", content.getCensorship());
        putJSON(obj, "content_type", isDoujinshi ? "DOUJINSHI" : "HENTAI_MANGA");
        putJSON(obj, "color", content.getColor());

        return obj;
    }

    /**
     * Create {@link JSONArray} object with special Atsumeru Serie/Archive hashes and put it into {@link JSONObject} metadata
     *
     * @param obj         {@link JSONObject} with metadata
     * @param serieHash   special Atsumeru hash that represents Serie uniq identifier
     * @param archiveHash special Atsumeru hash that represents Archive uniq identifier
     */
    private static void putHashes(JSONObject obj, String serieHash, String archiveHash) {
        JSONObject atsumeru = new JSONObject();
        putJSON(atsumeru, "serie_hash", serieHash);
        putJSON(atsumeru, "hash", archiveHash);

        obj.put("atsumeru", atsumeru);
    }

    /**
     * Create {@link JSONArray} object with {@link JSONObject} links and put it into {@link JSONObject} metadata
     *
     * @param obj  {@link JSONObject} with metadata
     * @param link online link for content in archive
     */
    private static void putLinks(JSONObject obj, String link) {
        JSONArray linksArray = new JSONArray();
        JSONObject linksObj = new JSONObject();
        putJSON(linksObj, "source", UrlUtils.getHostName(link));
        putJSON(linksObj, "link", link);
        linksArray.put(linksObj);

        if (!linksObj.isEmpty()) {
            obj.put("links", linksArray);
        }
    }

    /**
     * Put {@link Collection} into {@link JSONObject} metadata with given name
     *
     * @param obj        {@link JSONObject} with metadata
     * @param name       collection name
     * @param collection actual {@link Collection} with data
     */
    private static void putJSON(JSONObject obj, String name, Collection<?> collection) {
        if (ArrayUtils.isNotEmpty(collection)) {
            obj.put(name, collection);
        }
    }

    /**
     * Put {@link String} into {@link JSONObject} metadata with given name
     *
     * @param obj   {@link JSONObject} with metadata
     * @param name  string name
     * @param value actual {@link String} with data
     */
    private static void putJSON(JSONObject obj, String name, String value) {
        if (StringUtils.isNotEmpty(value)) {
            obj.put(name, value);
        }
    }
}
