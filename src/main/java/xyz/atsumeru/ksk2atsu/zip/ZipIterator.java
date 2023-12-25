package xyz.atsumeru.ksk2atsu.zip;

import net.greypanther.natsort.CaseInsensitiveSimpleNaturalComparator;
import xyz.atsumeru.ksk2atsu.utils.FileUtils;

import java.io.*;
import java.net.URI;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * {@link ZipFile} wrapper for easier archives reading and writing
 */
public class ZipIterator implements Closeable {
    private static final Comparator<String> natSortComparator = CaseInsensitiveSimpleNaturalComparator.getInstance();

    private final ZipFile zipFile;
    private ListIterator<? extends ZipEntry> iterator;
    private ZipEntry entry;

    /**
     * Open given {@link File} as {@link ZipFile} for reading and writing
     *
     * @param archive {@link File}
     * @throws IOException if an I/O error has occurred
     */
    private ZipIterator(File archive) throws IOException {
        zipFile = new ZipFile(archive, Charset.forName("CP866"));
        reset();
    }

    /**
     * Open given {@link File} as {@link ZipFile} for reading and writing and retrieving {@link ZipIterator} wrapper instance
     *
     * @param archive {@link File}
     * @return {@link ZipIterator} wrapper instance
     * @throws IOException if an I/O error has occurred
     */
    public static ZipIterator open(File archive) throws IOException {
        return new ZipIterator(archive);
    }

    /**
     * List all {@link ZipEntry} in {@link ZipFile}, naturally sort them and create {@link ListIterator}
     */
    public void reset() {
        List<? extends ZipEntry> entries = Collections.list(zipFile.entries());

        iterator = entries.stream()
                .filter(it -> !it.isDirectory())
                .sorted((entry1, entry2) -> natSortComparator.compare(entry1.getName().toLowerCase(), entry2.getName().toLowerCase()))
                .collect(Collectors.toList())
                .listIterator();
    }

    /**
     * Iterate over {@link ListIterator} with {@link ZipEntry} list
     *
     * @return true if next {@link ZipEntry} is present
     */
    public boolean next() {
        if (iterator.hasNext()) {
            entry = iterator.next();
            return true;
        }
        return false;
    }

    /**
     * Get current {@link ZipEntry} name. It may be directory or file name
     *
     * @return {@link ZipEntry} {@link String} name
     */
    public String getEntryName() {
        return entry.getName();
    }

    /**
     * Get {@link InputStream} from current {@link ZipEntry}
     *
     * @return {@link InputStream} from current {@link ZipEntry}
     * @throws IOException if an I/O error has occurred
     */
    public InputStream getEntryInputStream() throws IOException {
        return zipFile.getInputStream(entry);
    }

    /**
     * Save given {@link String} data from {@link Map} into archive
     *
     * @param archivePath            archive {@link String} path in filesystem
     * @param fileNameWithContentMap {@link Map} of values where key - filename in archive and value - actual file {@link String} content
     * @return true if content was saved
     */
    public boolean saveIntoArchive(String archivePath, Map<String, String> fileNameWithContentMap) {
        close();

        Map<String, String> env = new HashMap<>();
        env.put("create", "true");
        URI uri = URI.create("jar:" + Paths.get(archivePath).toUri());
        try (FileSystem fileSystem = FileSystems.newFileSystem(uri, env)) {
            for (Map.Entry<String, String> entry : fileNameWithContentMap.entrySet()) {
                Path nf = fileSystem.getPath(entry.getKey());
                try (Writer writer = Files.newBufferedWriter(nf, StandardCharsets.UTF_8, StandardOpenOption.CREATE)) {
                    writer.write(entry.getValue());
                }
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Close {@link ZipFile} and destroy {@link ListIterator} with {@link ZipEntry}
     */
    @Override
    public void close() {
        iterator = null;
        entry = null;
        FileUtils.closeQuietly(zipFile);
    }
}
