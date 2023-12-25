package xyz.atsumeru.ksk2atsu.managers;

import me.tongfei.progressbar.ProgressBar;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;
import xyz.atsumeru.ksk2atsu.database.models.Book;
import xyz.atsumeru.ksk2atsu.database.models.Content;
import xyz.atsumeru.ksk2atsu.metadata.BookInfo;
import xyz.atsumeru.ksk2atsu.metadata.FileMetadata;
import xyz.atsumeru.ksk2atsu.metadata.YAMLContent;
import xyz.atsumeru.ksk2atsu.utils.ComicUtils;
import xyz.atsumeru.ksk2atsu.utils.ProgressBarBuilder;
import xyz.atsumeru.ksk2atsu.utils.StringUtils;

import java.io.File;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class BooksRenamer {

    /**
     * Rename all archives in given input {@link File} directory creating new names from metadata and move Books from
     * {@link App#DOUJINS_FOLDER} into {@link App#BOOKS_FOLDER} if metadata for corresponding archive is in {@link Book} {@link Database} table
     *
     * @param outputDir output {@link File} dir where sorted archives is stored
     * @param database  link to {@link Database} object. Used for querying all data from {@link Book} table for later
     *                  checking if corresponding archive is Book
     * @return {@link List} of {@link String} errors
     */
    public static List<String> rename(File outputDir, Database database) {
        List<FileMetadata> filesMetadata = MetadataParser.parse(outputDir);
        Map<String, Content> booksByUrl = MetadataGenerator.getContentMapByUrl(database, CatalogType.BOOKS);
        ProgressBar progressBar = ProgressBarBuilder.create("Renaming files:", filesMetadata.size());

        List<String> errors = filesMetadata.stream()
                .peek(fileMetadata -> progressBar.step())
                .filter(fileMetadata -> !renameFile(fileMetadata, booksByUrl))
                .map(FileMetadata::getFile)
                .map(file -> "Unable to rename file: " + file)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        progressBar.close();

        return errors;
    }

    /**
     * Rename archive {@link File} using metadata info from {@link FileMetadata} by formula
     * <p>({@link BookInfo#getEvent()}) [{@link BookInfo#getAuthors()}] {@link BookInfo#getTitle()} (quality).cbz</p>
     *
     * @param fileMetadata {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata
     * @param booksByUrl   {@link Map} with content url as key, and {@link Content} book as value for checking if archive
     *                     will be moved into {@link App#BOOKS_FOLDER}
     * @return true if rename/moving was successful
     */
    private static boolean renameFile(FileMetadata fileMetadata, Map<String, Content> booksByUrl) {
        try {
            // It's book. Change directory from Doujins to Books
            String parentDir = fileMetadata.getFile().getParentFile().toString();
            if (booksByUrl.containsKey(fileMetadata.getBookInfo().getLink())) {
                parentDir = parentDir.replace(App.DOUJINS_FOLDER, App.BOOKS_FOLDER);
            }

            // Get optional metadata values from book_info and file name (event and quality)
            String event = Optional.ofNullable(fileMetadata.getBookInfo().getEvent())
                    .filter(StringUtils::isNotEmpty)
                    .map(value -> String.format("(%s) ", value))
                    .orElse("");

            String fileName = fileMetadata.getFile().getName();
            String quality = fileName.contains("x3200") || fileName.contains("x3199") || fileName.contains("x3100") ? " (x3200)" : "";

            // Create new file name using scheme: (event) [author] book name.cbz
            File newFile = new File(parentDir, ComicUtils.getTitleReplacedDeniedSymbols(String.format("%s[%s] %s%s.cbz", event, fileMetadata.getBookInfo().getAuthors(), fileMetadata.getBookInfo().getTitle(), quality)));

            // Create new potential dirs
            newFile.getParentFile().mkdirs();

            // Move file into new place with new name
            Files.move(fileMetadata.getFile().toPath(), newFile.toPath());
            return true;
        } catch (Exception e) {
            return e instanceof FileAlreadyExistsException;
        }
    }
}
