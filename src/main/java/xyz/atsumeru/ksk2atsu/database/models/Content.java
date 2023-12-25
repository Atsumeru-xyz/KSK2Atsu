package xyz.atsumeru.ksk2atsu.database.models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.NoArgsConstructor;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;
import xyz.atsumeru.ksk2atsu.utils.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Base model of {@link Content} table in {@link Database}. Corresponds to {@link CatalogType#UNLIMITED} value
 */
@Data
@NoArgsConstructor
@DatabaseTable(tableName = "content")
public class Content {
    @DatabaseField(columnName = "id", id = true)
    public Integer id;

    @DatabaseField(columnName = "url")
    public String url;
    @DatabaseField(columnName = "title")
    public String title;
    @DatabaseField(columnName = "author")
    public String author;
    @DatabaseField(columnName = "cover")
    public String cover;

    @DatabaseField(columnName = "artists")
    public String artists;
    @DatabaseField(columnName = "parodies")
    public String parodies;
    @DatabaseField(columnName = "circles")
    public String circles;
    @DatabaseField(columnName = "publisher")
    public String publisher;
    @DatabaseField(columnName = "event")
    public String event;
    @DatabaseField(columnName = "magazine")
    public String magazine;
    @DatabaseField(columnName = "language")
    public String language;
    @DatabaseField(columnName = "translator")
    public String translator;
    @DatabaseField(columnName = "tags")
    public String tags;
    @DatabaseField(columnName = "color")
    public String color;
    @DatabaseField(columnName = "censorship")
    public String censorship;

    @DatabaseField(columnName = "description")
    public String description;

    /**
     * Method for merging {@link List} of {@link Content} into one single {@link Content}. Used for creating uber-metadata
     * for single Atsumeru Serie
     *
     * @param title    Atsumeru Serie title
     * @param contents {@link List} of {@link Content} that will be merged
     * @return uber-metadata {@link Content}
     */
    public static Content merge(String title, List<Content> contents) {
        Content merged = new Content();
        merged.setTitle(title);
        merged.setLanguage("English");

        for (Content content : contents) {
            merged.author = merge(merged.author, content.author);
            merged.artists = merge(merged.artists, content.artists);
            merged.parodies = merge(merged.parodies, content.parodies);
            merged.circles = merge(merged.circles, content.circles);
            merged.publisher = merge(merged.publisher, content.publisher);
            merged.event = merge(merged.event, content.event);
            merged.magazine = merge(merged.magazine, content.magazine);
            merged.translator = merge(merged.translator, content.translator);
            merged.tags = merge(merged.tags, content.tags);
            if (StringUtils.equalsIgnoreCase(content.censorship, "UNCENSORED")) {
                merged.censorship = "UNCENSORED";
            }
            merged.description = Stream.of(merged.description, content.description)
                    .filter(StringUtils::isNotEmpty)
                    .collect(Collectors.joining("\n\n"));
        }
        return merged;
    }

    /**
     * Helper method for {@link Content#merge(String, List)} method that will merge two comma-separated {@link String}
     * into one without duplicates
     *
     * @param first  comma-separated {@link String}
     * @param second comma-separated {@link String}
     * @return comma-separated {@link String} from two other without duplicates
     */
    private static String merge(String first, String second) {
        return Stream.of(first, second)
                .filter(StringUtils::isNotEmpty)
                .flatMap(item -> Arrays.stream(item.split(",")))
                .map(String::trim)
                .distinct()
                .collect(Collectors.joining(","));
    }
}
