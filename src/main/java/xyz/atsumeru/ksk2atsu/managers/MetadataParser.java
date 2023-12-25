package xyz.atsumeru.ksk2atsu.managers;

import com.google.gson.Gson;
import me.tongfei.progressbar.ProgressBar;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import xyz.atsumeru.ksk2atsu.App;
import xyz.atsumeru.ksk2atsu.metadata.BookInfo;
import xyz.atsumeru.ksk2atsu.metadata.FileMetadata;
import xyz.atsumeru.ksk2atsu.metadata.YAMLContent;
import xyz.atsumeru.ksk2atsu.utils.FileUtils;
import xyz.atsumeru.ksk2atsu.utils.ProgressBarBuilder;
import xyz.atsumeru.ksk2atsu.utils.StringUtils;
import xyz.atsumeru.ksk2atsu.zip.ZipIterator;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Iterates over given {@link List} of {@link File} archives and parses {@link YAMLContent} and {@link BookInfo} metadata
 */
public class MetadataParser {
    private static final Yaml yaml = new Yaml(new Constructor(YAMLContent.class));
    private static final Gson gson = new Gson();

    /**
     * Parse {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata in {@link File} dir recursively
     *
     * @param dir input {@link File} dir
     * @return {@link List} of {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata
     */
    public static List<FileMetadata> parse(File dir) {
        List<File> files = FileUtils.listComicArchiveFiles(dir);
        ProgressBar progressBar = ProgressBarBuilder.create("Parsing metadata:", files.size());

        List<FileMetadata> list = files.stream()
                .peek(file -> progressBar.step())
                .filter(file -> {
                    // Check if given file is zip or cbz file by extension
                    String extension = FileUtils.getFileExtension(file).toLowerCase();
                    return extension.equalsIgnoreCase(App.ZIP_EXTENSION) || extension.equalsIgnoreCase(App.CBZ_EXTENSION);
                })
                .map(MetadataParser::readMetadata)
                .collect(Collectors.toList());

        progressBar.close();

        return list;
    }

    /**
     * Parse {@link YAMLContent} and {@link BookInfo} metadata and construct {@link FileMetadata} object
     *
     * @param zipFile zip {@link File} to read and parse
     * @return {@link FileMetadata} with {@link YAMLContent} and {@link BookInfo} metadata
     */
    private static FileMetadata readMetadata(File zipFile) {
        try (ZipIterator zipIterator = ZipIterator.open(zipFile)) {
            YAMLContent yamlContent = null;
            BookInfo bookInfo = null;
            while (zipIterator.next() && (yamlContent == null || bookInfo == null)) {
                // Check if entry is info.yaml metadata file
                String extension = FileUtils.getFileExtension(new File(zipIterator.getEntryName())).toLowerCase();
                if (extension.equals("yaml")) {
                    // Deserialize info.yaml into YAMLContent model
                    yamlContent = yaml.load(IOUtils.toString(zipIterator.getEntryInputStream(), StandardCharsets.UTF_8).replace("- - ", "  - "));
                    continue;
                }

                // Check if entry is book_info.json metadata file
                String fileName = zipIterator.getEntryName().toLowerCase();
                if (StringUtils.equalsIgnoreCase(fileName, App.BOOK_INFO_JSON)) {
                    // Deserialize book_info.json into BookInfo model
                    bookInfo = gson.fromJson(IOUtils.toString(zipIterator.getEntryInputStream(), StandardCharsets.UTF_8), BookInfo.class);
                }
            }

            // Construct FileMetadata from parsed YAMLContent and BookInfo
            return new FileMetadata(zipFile, yamlContent, bookInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new FileMetadata(zipFile, null, null);
    }
}
