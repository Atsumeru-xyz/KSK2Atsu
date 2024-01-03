package xyz.atsumeru.ksk2atsu.utils;

import xyz.atsumeru.ksk2atsu.App;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Small collection of {@link File} utils
 */
public class FileUtils {

    /**
     * Non-recursive list {@link File} dirs in given {@link File} dir
     *
     * @param dir input {@link File} dir
     * @return {@link List} of {@link File} dirs
     */
    public static List<Path> listDirs(File dir) {
        try (Stream<Path> stream = Files.list(dir.toPath())) {
            return stream.filter(path -> path.toFile().isDirectory())
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Recursively list all {@link File} Comic files with {@link App#ZIP_EXTENSION} or {@link App#CBZ_EXTENSION} extensions in given {@link File} dir
     *
     * @param dir input {@link File} dir
     * @return {@link List} of {@link File} Comic files
     */
    public static List<File> listComicArchiveFiles(File dir) {
        try (Stream<Path> stream = Files.walk(dir.toPath(), Integer.MAX_VALUE)) {
            return stream
                    .filter(path -> !Files.isDirectory(path))
                    .map(Path::toFile)
                    .filter(file -> {
                        String extension = FileUtils.getFileExtension(file).toLowerCase();
                        return extension.equalsIgnoreCase(App.ZIP_EXTENSION) || extension.equalsIgnoreCase(App.CBZ_EXTENSION);
                    })
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Get {@link File#getName()} but without extension
     *
     * @param file input {@link File}
     * @return {@link String} file name without extension
     */
    public static String getFileNameWithoutExtension(File file) {
        String fileName = file.getName();
        return fileName.substring(0, fileName.lastIndexOf(".")).trim();
    }

    /**
     * Get {@link File} extension from {@link File#getName()}
     *
     * @param file input {@link File}
     * @return {@link String} file extension
     */
    public static String getFileExtension(File file) {
        String fileName = file.getName();
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }

    /**
     * Write given {@link String} content into {@link File}
     *
     * @param file    destination {@link File}
     * @param content {@link String} content to write
     */
    public static void writeStringToFile(File file, String content) {
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(file);
            PrintWriter printWriter = new PrintWriter(fileWriter);
            printWriter.print(content);
            printWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeQuietly(fileWriter);
        }
    }

    /**
     * Quietly close {@link Closeable}
     *
     * @param closeable {@link Closeable} that will be closed quietly
     */
    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {
                // ignore
            }
        }
    }

    /**
     * Checks if given {@link File} directory is empty
     *
     * @param dir input {@link File} directory
     * @return true if empty
     */
    public static boolean isDirectoryEmpty(File dir) {
        try (Stream<Path> entries = Files.list(dir.toPath())) {
            return entries.findFirst().isEmpty();
        } catch (IOException e) {
            return false;
        }
    }
}
