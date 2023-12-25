package xyz.atsumeru.ksk2atsu.managers;

import me.tongfei.progressbar.ProgressBar;
import me.xdrop.fuzzywuzzy.FuzzySearch;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;
import xyz.atsumeru.ksk2atsu.database.models.Content;
import xyz.atsumeru.ksk2atsu.database.models.Doujin;
import xyz.atsumeru.ksk2atsu.metadata.BookInfo;
import xyz.atsumeru.ksk2atsu.metadata.FileMetadata;
import xyz.atsumeru.ksk2atsu.metadata.YAMLContent;
import xyz.atsumeru.ksk2atsu.utils.*;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MetadataGenerator {
    private static Map<File, FileMetadata> metadataByFile;
    private static Map<String, Content> contentMapByUrl;
    private static Map<String, Content> contentMapByName;

    private static Map<String, List<Content>> contentMapByMagazine;

    private static ProgressBar progressBar;

    /**
     * Iterate over all {@link File} in input {@link File} directory, parse metadata/name, match with metadata from
     * {@link Database}, convert metadata into {@link BookInfo} and save it in corresponding archive
     * <p>
     * This method generates metadata only for Comic Magazines
     *
     * @param inputDir input {@link File} directory
     * @param database link to {@link Database} object. Used for querying all data from all tables and matching files with metadata
     * @return {@link List} of {@link String} errors
     */
    public static List<String> generateForMagazines(File inputDir, Database database) {
        contentMapByMagazine = getContentMapByMagazine(database);

        List<File> files = FileUtils.listDirs(inputDir)
                .stream()
                .map(Path::toFile)
                .map(FileUtils::listDirs)
                .flatMap(Collection::stream)
                .map(Path::toFile)
                .toList();

        progressBar = ProgressBarBuilder.create("Magazines metadata:", files.size());

        List<String> list = files.stream()
                .map(MetadataGenerator::generateMagazineMetadata)
                .filter(ArrayUtils::isNotEmpty)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        progressBar.close();

        return list;
    }

    /**
     * Iterate over all {@link File} in input {@link File} directory, parse metadata/name, match with metadata from
     * {@link Database}, convert metadata into {@link BookInfo} and save it in corresponding archive
     * <p>
     * This method generates metadata only for Doujinshi and Books
     *
     * @param inputDir     input {@link File} directory
     * @param fileMetadata {@link List} of all {@link FileMetadata} for archives with optional {@link YAMLContent} metadata
     * @param database     link to {@link Database} object. Used for querying all data from all tables and matching files with metadata
     * @return {@link List} of {@link String} errors
     */
    public static List<String> generateForDoujinshi(File inputDir, List<FileMetadata> fileMetadata, Database database) {
        contentMapByUrl = new HashMap<>() {{
            putAll(getContentMapByUrl(database, CatalogType.UNLIMITED));
            putAll(getContentMapByUrl(database, CatalogType.DOUJIN));
            putAll(getContentMapByUrl(database, CatalogType.BOOKS));
            putAll(getContentMapByUrl(database, CatalogType.OTHER));
        }};

        contentMapByName = getContentMapByName(database);

        metadataByFile = fileMetadata.stream()
                .collect(Collectors.toMap(FileMetadata::getFile, Function.identity()));

        List<File> files = FileUtils.listDirs(inputDir)
                .stream()
                .map(Path::toFile)
                .map(FileUtils::listComicArchiveFiles)
                .flatMap(Collection::stream)
                .toList();

        progressBar = ProgressBarBuilder.create("Doujins/Books metadata:", files.size());

        List<String> list = files.stream()
                .filter(file -> !MetadataGenerator.generateDoujinMetadata(file))
                .map(File::toString)
                .collect(Collectors.toList());

        progressBar.close();

        return list;
    }

    /**
     * Matches {@link File} with {@link Content} in {@link Database} using Url, title with author or with {@link FuzzySearch}
     *
     * @param file input {@link File}
     * @return true if file matched with {@link Content} in {@link Database}
     */
    private static boolean generateDoujinMetadata(File file) {
        // Find book by url from YAML metadata
        Content content = findContentByUrl(file);
        if (content != null) {
            progressBar.step();
            saveBookMetadata(file, content);
            return true;
        }

        // Find book by title with author
        String titleWithAuthor = ComicUtils.getTitleWithAuthorAndReplacedDeniedSymbols(file.getName().toLowerCase());
        content = contentMapByName.get(titleWithAuthor);
        if (content != null) {
            progressBar.step();
            saveBookMetadata(file, content);
            return true;
        }

        // Find book by title using Fuzzy search
        content = findContentByCleanedTitleFuzzy(titleWithAuthor);
        if (content != null) {
            progressBar.step();
            saveBookMetadata(file, content);
            return true;
        }

        // Unable to find content in database
        progressBar.step();
        return false;
    }

    /**
     * Get {@link Content} from {@link Database} by Url
     *
     * @param file input {@link File}
     * @return matched {@link Content} from {@link Database} or null
     */
    private static Content findContentByUrl(File file) {
        String bookUrl = metadataByFile.get(file).getUrl();
        if (StringUtils.isNotEmpty(bookUrl)) {
            return contentMapByUrl.get(bookUrl.toLowerCase());
        }
        return null;
    }

    /**
     * Find {@link Content} from {@link Database} cleaned title with author using {@link FuzzySearch}
     *
     * @param titleWithAuthor input {@link String} title with author
     * @return matched {@link Content} from {@link Database} or null
     */
    private static Content findContentByCleanedTitleFuzzy(String titleWithAuthor) {
        String cleanedTitle = ComicUtils.fixKnownTitleIssues(titleWithAuthor.replaceAll("^\\[.*?]", "").trim());
        return contentMapByName.entrySet()
                .stream()
                .map(entry -> new Pair<>(StringUtils.getStringsSimilarity(
                        entry.getKey().replaceAll("^\\[.*?]", "").trim(),
                        cleanedTitle
                ), entry.getValue()))
                .filter(pair -> pair.first >= 85)
                .max(Comparator.comparingInt(pair -> pair.first))
                .map(pair -> pair.second)
                .orElse(null);
    }

    /**
     * Create {@link BookInfo} metadata from {@link Content} and save it into archive {@link File}
     *
     * @param file    input {@link File}
     * @param content matched {@link Content} from {@link Database}
     */
    private static void saveBookMetadata(File file, Content content) {
        BookInfo.saveIntoArchive(
                file,
                content,
                StringUtils.md5Hex(content.getUrl()),
                createContentHash(file),
                false,
                content instanceof Doujin
        );
    }

    /**
     * Matches {@link File} with {@link Content} in {@link Database} using Magazine name and Issue
     *
     * @param archiveFile input {@link File}
     * @return {@link List} of {@link String} errors
     */
    private static List<String> generateMagazineMetadata(File archiveFile) {
        // Generate metadata for each file
        String fileName = archiveFile.getName().toLowerCase();
        String folderName = archiveFile.getParentFile().getName().toLowerCase();
        List<Content> magazineContent = contentMapByMagazine.get(folderName);
        if (magazineContent == null) {
            progressBar.step();
            return List.of("Unable to detect magazine: " + fileName);
        }

        String serieHash = createSerieHash(archiveFile);

        List<String> errors = new ArrayList<>();
        List<Content> contents = new ArrayList<>();
        for (File file : FileUtils.listComicArchiveFiles(archiveFile)) {
            Pair<Content, String> contentPair = getContentFromFile(magazineContent, file);
            Content content = contentPair.first;
            if (content != null) {
                BookInfo.saveIntoArchive(
                        file,
                        content,
                        serieHash,
                        createContentHash(file),
                        false,
                        false
                );

                contents.add(content);
            } else {
                errors.add(contentPair.second);
            }
        }

        BookInfo.saveToFile(
                archiveFile.getPath() + File.separator + App.SERIE_INFO_JSON,
                Content.merge(folderName, contents),
                serieHash,
                null,
                true,
                false
        );

        progressBar.step();

        return errors;
    }

    /**
     * Match {@link Content} for Issue name from {@link List} of Magazine {@link Content}
     *
     * @param magazineContent {@link List} of Magazine {@link Content}
     * @param file            input {@link File}
     * @return {@link Pair} of {@link Content} or {@link String} error
     */
    private static Pair<Content, String> getContentFromFile(List<Content> magazineContent, File file) {
        String issueName = ComicUtils.fixKnowMagazineIssueTitleIssues(
                ComicUtils.getCleanedTitle(
                        FileUtils.getFileNameWithoutExtension(file)
                                .toLowerCase()
                                .replace("(1)", "")
                                .replace("＂", "\"")
                                .replaceAll(" (\\(([^)]+)\\))$", "")
                )
        );
        return magazineContent.stream()
                .filter(content1 -> StringUtils.equalsIgnoreCase(content1.getTitle(), issueName))
                .findFirst()
                .map(content -> new Pair<Content, String>(content, null))
                .orElseGet(() -> new Pair<>(null, "Unable to find content: " + file + ". Issue name: [" + issueName + "]"));
    }

    /**
     * Generate Atsumeru Serie hash for file
     *
     * @param file input {@link File}
     * @return {@link String} Atsumeru Serie hash
     */
    private static String createSerieHash(File file) {
        return App.SERIE_HASH_TAG + StringUtils.md5Hex(App.APP_NAME + file.getName().toLowerCase());
    }

    /**
     * Generate Atsumeru Archive hash for file
     *
     * @param file input {@link File}
     * @return {@link String} Atsumeru Archive hash
     */
    private static String createContentHash(File file) {
        return App.ARCHIVE_HASH_TAG + StringUtils.md5Hex(App.APP_NAME + file.getName().toLowerCase());
    }

    /**
     * Load all {@link Content} for {@link CatalogType#UNLIMITED} from {@link Database} and map it into {@link Map}
     * where key - Comic Magazine {@link String} and value - {@link List} of {@link Content} in that Magazine
     *
     * @param database link to {@link Database} object
     * @return {@link Map} where key - Comic Magazine {@link String} and value - {@link List} of {@link Content} in that Magazine
     */
    private static Map<String, List<Content>> getContentMapByMagazine(Database database) {
        return database.getDao()
                .queryAll(CatalogType.UNLIMITED)
                .stream()
                .filter(content -> StringUtils.isNotEmpty(content.getMagazine()))
                .filter(content -> !StringUtils.equalsIgnoreCase(content.getMagazine(), App.FAKKU))
                .map(Content.class::cast)
                .filter(content -> ComicUtils.detectComicNameAndIssue(content.getMagazine().split(",")[0].trim(), 0) != null)
                .collect(Collectors.groupingBy(
                        content -> ComicUtils.detectComicNameAndIssue(content.getMagazine().split(",")[0].trim(), 0).first.toLowerCase(),
                        Collectors.toList()
                ));
    }

    /**
     * Load all {@link Content} for {@link CatalogType} from {@link Database} and map it into {@link Map}
     * where key - {@link Content} url and value - {@link Content} for that url
     *
     * @param database    link to {@link Database} object
     * @param catalogType {@link CatalogType} for loading data from {@link Database}
     * @return {@link Map} where key - {@link Content} url and value - {@link Content} for that url
     */
    public static Map<String, Content> getContentMapByUrl(Database database, CatalogType catalogType) {
        return database.getDao()
                .queryAll(catalogType)
                .stream()
                .map(Content.class::cast)
                .collect(Collectors.toMap(
                        content -> content.getUrl().toLowerCase(),
                        Function.identity()
                ));
    }

    /**
     * Load all {@link Content} from {@link Database} and map it into {@link Map} where key - {@link Content} title
     * and value - {@link Content} for that title
     *
     * @param database link to {@link Database} object
     * @return {@link Map} where key - {@link Content} title and value - {@link Content} for that title
     */
    private static Map<String, Content> getContentMapByName(Database database) {
        return Stream.concat(
                Stream.concat(
                        getContentMap(database, CatalogType.BOOKS).entrySet().stream(),
                        getContentMap(database, CatalogType.DOUJIN).entrySet().stream()
                ),
                Stream.concat(
                        getContentMap(database, CatalogType.UNLIMITED).entrySet().stream(),
                        getContentMap(database, CatalogType.OTHER).entrySet().stream()
                )
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (key1, key2) -> key1));
    }

    /**
     * Load all {@link Content} for {@link CatalogType} from {@link Database} and map it into {@link Map}
     * where key - {@link Content} title and value - {@link Content} for that title
     *
     * @param database link to {@link Database} object
     *                 * @param catalogType {@link CatalogType} for loading data from {@link Database}
     * @return {@link Map} where key - {@link Content} title and value - {@link Content} for that title
     */
    private static Map<String, Content> getContentMap(Database database, CatalogType catalogType) {
        return database.getDao()
                .queryAll(catalogType)
                .stream()
                .collect(Collectors.toMap(content -> {
                    String magazine = Optional.ofNullable(content.getMagazine())
                            .filter(StringUtils::isNotEmpty)
                            .map(str -> str.split(",")[0].trim())
                            .orElse("");

                    String comicWithIssue = Optional.ofNullable(ComicUtils.detectComicNameAndIssue(magazine, 0))
                            .map(pair -> ComicUtils.getComicWithIssueName(pair.first, pair.second))
                            .orElse("");

                    String titleWithArtist = Optional.ofNullable(content.getArtists())
                            .filter(StringUtils::isNotEmpty)
                            .map(ComicUtils::getArtistReplacedDeniedSymbols)
                            .map(artists -> ComicUtils.getTitleWithArtistAndReplacedDeniedSymbols(content.getTitle(), artists))
                            .orElseGet(() -> ComicUtils.getTitleWithArtistAndReplacedDeniedSymbols(content.getTitle(), ""));

                    return String.format("%s - %s", comicWithIssue, titleWithArtist).toLowerCase()
                            .replaceAll("^ - ", "")
                            .trim();
                }, Function.identity()));
    }
}
