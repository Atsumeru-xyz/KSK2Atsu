package xyz.atsumeru.ksk2atsu;

import de.codeshelf.consoleui.elements.ConfirmChoice;
import de.codeshelf.consoleui.prompt.*;
import de.codeshelf.consoleui.prompt.builder.PromptBuilder;
import jline.TerminalFactory;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.BooksReSortingType;
import xyz.atsumeru.ksk2atsu.database.enums.MigrationType;
import xyz.atsumeru.ksk2atsu.managers.*;
import xyz.atsumeru.ksk2atsu.metadata.FileMetadata;
import xyz.atsumeru.ksk2atsu.utils.ArrayUtils;
import xyz.atsumeru.ksk2atsu.utils.FileUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class App {
    public static final String APP_NAME = "KSK2Atsu";
    public static final String APP_VERSION = "v1.1";

    public static final String ZIP_EXTENSION = "zip";
    public static final String CBZ_EXTENSION = "cbz";

    public static final String ARCHIVE_HASH_TAG = "atsumeru";
    public static final String SERIE_HASH_TAG = "atsumeru-serie";

    public static final String BOOK_INFO_JSON = "book_info.json";
    public static final String SERIE_INFO_JSON = "serie_info.json";

    public static final String BOOKS_FOLDER = "Books";
    public static final String DOUJINS_FOLDER = "Doujins";
    public static final String MAGAZINES_FOLDER = "Magazines";

    public static final String UNKNOWN = "Unknown";
    public static final String FAKKU = "FAKKU";
    public static final String ORIGINAL_WORK = "Original Work";

    private static final String DEBUG_ARG = "--debug";

    public static boolean IS_DEBUG = true;
    private static Map<String, String> argsMap;

    /**
     * App's main method. Configures console installing JANSI support and requesting some answers from user
     *
     * @param args app arguments. Unused
     */
    public static void main(String[] args) {
        // Parse arguments
        parseArgs(args);

        IS_DEBUG = Optional.ofNullable(argsMap.get(DEBUG_ARG))
                .map(Boolean::valueOf)
                .orElse(false);

        // Install ANSI console support
        AnsiConsole.systemInstall();

        // Warn if in debug mode
        if (App.IS_DEBUG) {
            System.err.println("WARNING! Running in Debug mode!");
        }

        // Print description message
        System.out.println(
                Ansi.ansi()
                        .eraseScreen()
                        .reset()
                        .render(APP_NAME)
                        .render(" ")
                        .render(APP_VERSION)
                        .render("\n\n")
                        .render("Simple tool that helps you migrate your KSK (Koushoku) rip into Atsumeru-ready dump organized by Magazines/Doujins/Books and filled with metadata")
                        .render("\n")
        );

        // Ask questions
        HashMap<String, ? extends PromtResultItemIF> result = prompt();

        // Get answers
        InputResult inputFolder = (InputResult) result.get("input_folder");
        InputResult outputFolder = (InputResult) result.get("output_folder");
        ListResult migrationType = (ListResult) result.get("migration_type");
        ListResult booksReSortingType = (ListResult) result.get("resorting_type");
        ConfirmResult rewriteMetadata = (ConfirmResult) result.get("rewrite_metadata");
        ConfirmResult isStart = (ConfirmResult) result.get("start");

        if (isStart.getConfirmed() == ConfirmChoice.ConfirmationValue.YES) {
            doTasks(
                    inputFolder.getInput(),
                    outputFolder.getInput(),
                    MigrationType.valueOf(migrationType.getSelectedId().toUpperCase()),
                    BooksReSortingType.valueOf(booksReSortingType.getSelectedId().toUpperCase()),
                    rewriteMetadata.getConfirmed() == ConfirmChoice.ConfirmationValue.YES
            );
            pressAnyKeyToClose();
        }

        System.exit(0);
    }

    /**
     * Request from user answers for some questions
     *
     * @return answers {@link Map} where key - prompt id and value - answer
     */
    private static HashMap<String, ? extends PromtResultItemIF> prompt() {
        ConsolePrompt prompt = new ConsolePrompt();
        PromptBuilder promptBuilder = prompt.getPromptBuilder();

        promptBuilder.createInputPrompt()
                .name("input_folder")
                .message("Enter folder path to your \"ksk rip\" folder")
                .addPrompt();

        promptBuilder.createInputPrompt()
                .name("output_folder")
                .message("Enter folder path where result will be stored")
                .addPrompt();

        promptBuilder.createListPrompt()
                .name("migration_type")
                .message("Migration type")
                .newItem("move").text("Move files (requires at least 5GB of free space)").add()
                .newItem("copy").text("Copy files (requires at least as much free space as the size of the files plus 5GB)").add()
                .addPrompt();

        promptBuilder.createListPrompt()
                .name("resorting_type")
                .message("Books and Doujinshi resorting type")
                .newItem("by_publisher").text("Put output files into folders by Publisher").add()
                .newItem("by_author").text("Put output files into folders by Author").add()
                .addPrompt();

        promptBuilder.createConfirmPromp()
                .name("rewrite_metadata")
                .message("Rewrite metadata in archives if metadata already present? (default: no)")
                .defaultValue(ConfirmChoice.ConfirmationValue.NO)
                .addPrompt();

        promptBuilder.createConfirmPromp()
                .name("start")
                .message("Start migration? It can take ~2-6 hours to complete depending on your hardware and migration type (default: yes)")
                .defaultValue(ConfirmChoice.ConfirmationValue.YES)
                .addPrompt();

        try {
            return prompt.prompt(promptBuilder.build());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                TerminalFactory.get().restore();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Do all needed migrating tasks
     *
     * @param input           {@link File} dir with ksk rip files
     * @param output          {@link File} dir where result will be stored
     * @param migrationType   if {@link MigrationType#MOVE}, files from input dir will be moved into output, otherwise copied
     * @param reSortingType   if {@link BooksReSortingType#BY_AUTHOR}, files in output dir will be resorted by Author, otherwise by Publisher
     * @param reWriteMetadata if true, metadata will be regenerated and rewrote into archive file even if present
     */
    private static void doTasks(String input, String output, MigrationType migrationType, BooksReSortingType reSortingType, boolean reWriteMetadata) {
        System.out.println();

        // Connect to metadata dump database
        Database database = new Database();

        // Working and output dir
        File workingDir = new File(input);
        File outputDir = new File(output);

        File doujinsDir = new File(outputDir, DOUJINS_FOLDER);
        File magazinesDir = new File(outputDir, MAGAZINES_FOLDER);

        // Change all extensions in dump from *.zip to *.cbz
        ExtensionChanger.change(workingDir, ZIP_EXTENSION, CBZ_EXTENSION);

        // Move all books into a new place depending on parsed metadata
        List<String> booksMoveErrors = BooksMover.move(workingDir, outputDir, migrationType);

        // Download covers for magazines
        List<String> coverDownloadErrors = CoversDownloader.download(magazinesDir);

        // Parse metadata from files in new place
        List<FileMetadata> movedFiles = MetadataParser.parse(outputDir);

        // Generate metadata for each Magazine
        List<String> metadataGenerateForMagazinesErrors = MetadataGenerator.generateForMagazines(magazinesDir, movedFiles, database, reWriteMetadata);

        // Generate metadata for each Book
        List<String> metadataGenerateForBooksErrors = MetadataGenerator.generateForDoujinshi(doujinsDir, movedFiles, database, reWriteMetadata);

        // Rename all books using saved metadata
        List<String> renameErrors = BooksRenamer.rename(outputDir, reSortingType);

        saveLogs(booksMoveErrors, coverDownloadErrors, metadataGenerateForMagazinesErrors, metadataGenerateForBooksErrors, renameErrors);
        database.close();
    }

    /**
     * Save all error logs into file and open it in Notepad
     *
     * @param booksMoveErrors                    errors from {@link BooksMover}
     * @param coverDownloadErrors                errors from {@link CoversDownloader}
     * @param metadataGenerateForMagazinesErrors errors from {@link MetadataGenerator#generateForMagazines(File, List, Database, boolean)}
     * @param metadataGenerateForBooksErrors     errors from {@link MetadataGenerator#generateForDoujinshi(File, List, Database, boolean)}
     * @param renameErrors                       errors from {@link BooksRenamer}
     */
    private static void saveLogs(List<String> booksMoveErrors, List<String> coverDownloadErrors, List<String> metadataGenerateForMagazinesErrors,
                                 List<String> metadataGenerateForBooksErrors, List<String> renameErrors) {
        List<String> errors = new ArrayList<>();
        if (ArrayUtils.isNotEmpty(booksMoveErrors)) {
            errors.add("Unable to move or copy files:");
            errors.addAll(booksMoveErrors);
            errors.add("\n");
        }

        if (ArrayUtils.isNotEmpty(coverDownloadErrors)) {
            errors.add("Unable to download covers for:");
            errors.addAll(coverDownloadErrors);
            errors.add("\n");
        }

        if (ArrayUtils.isNotEmpty(metadataGenerateForMagazinesErrors)) {
            errors.add("Unable to generate metadata for Magazines:");
            errors.addAll(metadataGenerateForMagazinesErrors);
            errors.add("\n");
        }

        if (ArrayUtils.isNotEmpty(metadataGenerateForBooksErrors)) {
            errors.add("Unable to generate metadata for Books:");
            errors.addAll(metadataGenerateForBooksErrors);
            errors.add("\n");
        }

        if (ArrayUtils.isNotEmpty(renameErrors)) {
            errors.add("Unable to rename files:");
            errors.addAll(renameErrors);
            errors.add("\n");
        }

        File errorsFile = new File("./errors.log");
        FileUtils.writeStringToFile(errorsFile, String.join("\n", errors));

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().edit(errorsFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.err.println("Error logs are saved into [" + errorsFile + "] file");
        }
    }

    /**
     * Helper method that blocks thread and requests hitting Enter after {@link #doTasks(String, String, MigrationType, BooksReSortingType, boolean)} finished working
     */
    private static void pressAnyKeyToClose() {
        System.out.println(Ansi.ansi().render("\nAll done! Press Enter to close app..."));
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseArgs(String[] args) {
        argsMap = Arrays.stream(args).collect(Collectors.toMap(arg -> arg.replaceAll("=.*", ""), arg -> arg.replaceAll("--.*=", "")));
    }
}
