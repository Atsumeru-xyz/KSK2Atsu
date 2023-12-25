package xyz.atsumeru.ksk2atsu.managers;

import me.tongfei.progressbar.ProgressBar;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.metadata.BookInfo;
import xyz.atsumeru.ksk2atsu.metadata.FileMetadata;
import xyz.atsumeru.ksk2atsu.metadata.YAMLContent;
import xyz.atsumeru.ksk2atsu.utils.ProgressBarBuilder;
import xyz.atsumeru.ksk2atsu.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BooksMover {

    /**
     * Iterate over archives in directory, parses metadata, Comic Magazine, Issue and resorts them into corresponding directories
     *
     * @param workingDir input {@link File} dir with files
     * @param outputDir  output {@link File} dir where result will be stored
     * @param isMove     if true, files from input dir will be moved into output, otherwise copied
     * @return {@link List} of {@link FileMetadata} parsed from moved and resorted files
     */
    public static List<FileMetadata> move(File workingDir, File outputDir, boolean isMove) {
        List<FileMetadata> fileMetadataList = MetadataParser.parse(workingDir);
        ProgressBar progressBar = ProgressBarBuilder.create("Moving files:", fileMetadataList.size());

        for (FileMetadata fileMetadata : fileMetadataList) {
            progressBar.step();
            moveOrCopyFile(fileMetadata, createNewFolder(outputDir, fileMetadata), isMove);
        }
        progressBar.close();

        return MetadataParser.parse(outputDir);
    }

    /**
     * Generate new directory name from {@link FileMetadata} parsed from archive {@link File}. By default, it tries to
     * create new name depending on {@link FileMetadata#getMagazine()} field from metadata, then from {@link FileMetadata#getPublisher()}
     * field and then fallback to {@link App#UNKNOWN} constant
     * <p> <p>
     * If {@link FileMetadata#getMagazine()} field present, new directory will be created using formula {@link App#MAGAZINES_FOLDER}/{@link FileMetadata#getMagazineName(String)}/{@link FileMetadata#getMagazineIssue(String)}
     * <p>
     * If {@link FileMetadata#getPublisher()} field present, new directory will be created using formula {@link App#DOUJINS_FOLDER}/{@link FileMetadata#getPublisher()}
     * <p>
     * Otherwise, new directory will be created using formula {@link App#DOUJINS_FOLDER}/{@link App#UNKNOWN}
     *
     * @param outputDir    output {@link File} dir where result will be stored
     * @param fileMetadata {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata
     * @return {@link File} that point to a new directory
     */
    private static File createNewFolder(File outputDir, FileMetadata fileMetadata) {
        File newDir;
        String magazine = fileMetadata.getMagazine();
        if (StringUtils.isNotEmpty(magazine)) {
            String magazineName = fileMetadata.getMagazineName(magazine);
            String magazineIssue = fileMetadata.getMagazineIssue(magazine);
            String magazineWithIssue = String.format("%s %s", magazineName, magazineIssue);

            newDir = magazineWithIssue.contains(App.FAKKU)
                    ? createFileForPublisher(outputDir, magazineIssue)
                    : new File(outputDir, App.MAGAZINES_FOLDER + File.separator + magazineName + File.separator + magazineWithIssue);
        } else if (StringUtils.isNotEmpty(fileMetadata.getPublisher())) {
            newDir = createFileForPublisher(outputDir, fileMetadata.getPublisher());
        } else {
            newDir = createFileForPublisher(outputDir, App.UNKNOWN);
        }
        newDir.mkdirs();
        return newDir;
    }

    /**
     * Create new directory using formula {@link App#DOUJINS_FOLDER}/{@link FileMetadata#getPublisher()}
     *
     * @param outputDir output {@link File} dir where result will be stored
     * @param publisher {@link FileMetadata#getPublisher()} value
     * @return {@link File} that point to a new directory
     */
    private static File createFileForPublisher(File outputDir, String publisher) {
        return new File(outputDir, App.DOUJINS_FOLDER + File.separator + publisher);
    }

    /**
     * {@link Files#move(Path, Path, CopyOption...)} or {@link Files#copy(Path, OutputStream)} file from old destination into new place
     * <p>
     * All other non-ksk rip archives will be deleted automatically. List of name rules are predefined
     *
     * @param fileMetadata {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata
     * @param newDir       destination {@link File} directory
     * @param isMove       if true, files from input dir will be moved into output, otherwise copied
     */
    private static void moveOrCopyFile(FileMetadata fileMetadata, File newDir, boolean isMove) {
        File newFile = new File(newDir, fileMetadata.getFile().getName());
        try {
            if (deleteOtherFiles(fileMetadata)) {
                return;
            }

            if (isMove) {
                Files.move(fileMetadata.getFile().toPath(), newFile.toPath());
            } else {
                Files.copy(fileMetadata.getFile().toPath(), newFile.toPath());
            }
        } catch (IOException e) {
            System.err.println("Unable to move or copy [" + fileMetadata.getFile() + "] to [" + newFile);
        }
    }

    /**
     * Deletes some archives that not related to ksk rip. List of name rules are predefined
     *
     * @param fileMetadata {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata
     * @return true if {@link File} was deleted
     */
    private static boolean deleteOtherFiles(FileMetadata fileMetadata) {
        String fileName = fileMetadata.getFile().getName();
        if (fileName.toLowerCase().contains("naked_daily_life") || fileName.toLowerCase().contains("the_program_of_pregnancy")) {
            return fileMetadata.getFile().delete();
        }
        return false;
    }
}
