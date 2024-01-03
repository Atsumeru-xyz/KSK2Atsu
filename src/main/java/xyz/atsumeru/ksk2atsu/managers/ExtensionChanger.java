package xyz.atsumeru.ksk2atsu.managers;

import me.tongfei.progressbar.ProgressBar;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.utils.FileUtils;
import xyz.atsumeru.ksk2atsu.utils.ProgressBarBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiPredicate;

public class ExtensionChanger {
    private static final BiPredicate<File, String> FILE_EXTENSION_PREDICATE = (file, extension) -> file.getName().toLowerCase().endsWith(extension.toLowerCase());

    /**
     * Iterate over archives in {@link File} directory and change extension from one into another
     *
     * @param dir        input {@link File} dir
     * @param changeFrom change from {@link String} extension
     * @param changeTo   change into {@link String} extension
     */
    public static void change(File dir, String changeFrom, String changeTo) {
        List<File> files = FileUtils.listComicArchiveFiles(dir);
        ProgressBar progressBar = ProgressBarBuilder.create("Changing extensions:", files.size());

        files.stream()
                .peek(file -> progressBar.step())
                .filter(file -> FILE_EXTENSION_PREDICATE.test(file, changeFrom))
                .forEach(file -> change(file, changeTo));

        progressBar.close();
    }

    /**
     * Change {@link File} extension
     *
     * @param file     input {@link File}
     * @param changeTo change into {@link String} extension
     */
    private static void change(File file, String changeTo) {
        try {
            Path newFilePath = new File(file.getParent(), changeExtension(file, changeTo)).toPath();
            Files.move(file.toPath(), newFilePath);
        } catch (IOException e) {
            if (App.IS_DEBUG) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Change {@link File#getName()} extension and return new name
     *
     * @param file         input {@link File}
     * @param newExtension new {@link String} extension
     * @return
     */
    private static String changeExtension(File file, String newExtension) {
        return FileUtils.getFileNameWithoutExtension(file) + "." + newExtension.toLowerCase();
    }
}
