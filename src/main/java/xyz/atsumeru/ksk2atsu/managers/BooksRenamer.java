package xyz.atsumeru.ksk2atsu.managers;

import me.tongfei.progressbar.ProgressBar;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.BooksReSortingType;
import xyz.atsumeru.ksk2atsu.database.models.Book;
import xyz.atsumeru.ksk2atsu.metadata.BookInfo;
import xyz.atsumeru.ksk2atsu.metadata.FileMetadata;
import xyz.atsumeru.ksk2atsu.metadata.YAMLContent;
import xyz.atsumeru.ksk2atsu.utils.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BooksRenamer {

    /**
     * Rename all archives in given input {@link File} directory creating new names from metadata and move Books from
     * {@link App#DOUJINS_FOLDER} into {@link App#BOOKS_FOLDER} if metadata for corresponding archive is in {@link Book} {@link Database} table
     *
     * @param outputDir     output {@link File} dir where sorted archives is stored
     * @param reSortingType if {@link BooksReSortingType#BY_AUTHOR}, files in output dir will be resorted by Author, otherwise by Publisher
     * @return {@link List} of {@link String} errors
     */
    public static List<String> rename(File outputDir, BooksReSortingType reSortingType) {
        List<FileMetadata> filesMetadata = MetadataParser.parse(outputDir);
        ProgressBar progressBar = ProgressBarBuilder.create("Renaming files:", filesMetadata.size());

        List<String> errors = filesMetadata.stream()
                .peek(fileMetadata -> progressBar.step())
                .filter(fileMetadata -> !renameFile(fileMetadata, reSortingType))
                .map(FileMetadata::getFile)
                .map(file -> "Unable to rename file: " + file)
                .filter(StringUtils::isNotEmpty)
                .collect(Collectors.toList());

        cleanOutputFolder(outputDir);

        progressBar.close();

        return errors;
    }

    /**
     * Rename archive {@link File} using metadata info from {@link FileMetadata} by formula
     * <p>({@link BookInfo#getEvent()}) [{@link BookInfo#getAuthors()}] {@link BookInfo#getTitle()} (quality).cbz</p>
     *
     * @param fileMetadata  {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata
     * @param reSortingType if {@link BooksReSortingType#BY_AUTHOR}, files in output dir will be resorted by Author, otherwise by Publisher
     * @return true if rename/moving was successful
     */
    private static boolean renameFile(FileMetadata fileMetadata, BooksReSortingType reSortingType) {
        try {
            File parentDir = fileMetadata.getFile().getParentFile();
            if (reSortingType == BooksReSortingType.BY_AUTHOR) {
                parentDir = new File(
                        parentDir.getParentFile(),
                        Optional.ofNullable(fileMetadata.getBookInfo())
                                .map(BookInfo::getAuthors)
                                .filter(StringUtils::isNotEmpty)
                                .map(ComicUtils::getArtistReplacedDeniedSymbols)
                                .orElse(App.UNKNOWN)
                );
            }

            // It's book. Change directory from Doujins to Books
            boolean isBook = Optional.ofNullable(fileMetadata.getBookInfo())
                    .map(BookInfo::getParodies)
                    .filter(ArrayUtils::isNotEmpty)
                    .map(parodies -> parodies.stream().anyMatch(parody -> StringUtils.equalsIgnoreCase(parody, App.ORIGINAL_WORK)))
                    .orElse(false);

            if (isBook) {
                parentDir = new File(parentDir.toString().replace(App.DOUJINS_FOLDER, App.BOOKS_FOLDER));
            }

            // Get optional metadata values from book_info and file name (event and quality)
            String event = Optional.ofNullable(fileMetadata.getBookInfo())
                    .map(BookInfo::getEvent)
                    .filter(StringUtils::isNotEmpty)
                    .map(value -> String.format("(%s) ", value))
                    .orElse("");

            String fileName = fileMetadata.getFile().getName();
            String quality = fileName.contains("x3200") || fileName.contains("x3199") || fileName.contains("x3100") ? " (x3200)" : "";

            // Create new file name using scheme: (event) [author] book name.cbz
            String author = Optional.ofNullable(fileMetadata.getBookInfo())
                    .map(BookInfo::getAuthors)
                    .filter(StringUtils::isNotEmpty)
                    .orElseGet(
                            () -> Optional.ofNullable(fileMetadata.getYamlContent())
                                    .map(YAMLContent::getArtist)
                                    .filter(ArrayUtils::isNotEmpty)
                                    .map(list -> list.get(0))
                                    .orElse(App.UNKNOWN)
                    );

            String title = Optional.ofNullable(fileMetadata.getBookInfo())
                    .map(BookInfo::getTitle)
                    .filter(StringUtils::isNotEmpty)
                    .orElseGet(
                            () -> Optional.ofNullable(fileMetadata.getYamlContent())
                                    .map(YAMLContent::getTitle)
                                    .filter(StringUtils::isNotEmpty)
                                    .orElseGet(() -> fileName.replace(".cbz", ""))
                    );

            File newFile = new File(parentDir, ComicUtils.getTitleReplacedDeniedSymbols(String.format("%s[%s] %s%s.cbz", event, author, title, quality)));

            // Create new potential dirs
            newFile.getParentFile().mkdirs();

            // Move file into new place with new name
            Files.move(fileMetadata.getFile().toPath(), newFile.toPath());
            return true;
        } catch (Exception e) {
            if (App.IS_DEBUG) {
                e.printStackTrace();
            }
            return e instanceof FileAlreadyExistsException;
        }
    }

    /**
     * Cleans output {@link File} directory by removing empty directories
     *
     * @param outputDir output {@link File} dir where sorted archives is stored
     */
    private static void cleanOutputFolder(File outputDir) {
        try (Stream<Path> stream = Files.walk(outputDir.toPath())) {
            stream.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .filter(File::isDirectory)
                    .filter(FileUtils::isDirectoryEmpty)
                    .forEach(File::delete);
        } catch (IOException e) {
            System.err.println("Unable to delete directory!");
        }
    }
}
