package xyz.atsumeru.ksk2atsu.database;

import lombok.Getter;
import org.apache.commons.io.IOUtils;
import xyz.atsumeru.ksk2atsu.App;

import java.io.*;
import java.sql.SQLException;
import java.util.Optional;

/**
 * Database connection, closing and providing access to {@link DaoManager}
 */
public class Database implements Closeable {
    private static final String DB_FILE_NAME = "dump.db";
    @Getter
    private DaoManager dao;

    /**
     * Create Database instance and connect to database
     */
    public Database() {
        connect();
    }

    /**
     * Unpack database from resources and put into launch folder. It's necessary to unpack it rather to direct using db
     * from resources because, for some reasons, file if null in runtime
     *
     * @return database {@link File}
     */
    private static File unpackDatabase() {
        File file = getDBFile();
        if (!file.exists()) {
            try (InputStream is = App.class.getClassLoader().getResourceAsStream(DB_FILE_NAME);
                 OutputStream out = new FileOutputStream(file)) {
                IOUtils.copy(is, out);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return file;
    }

    /**
     * Get database {@link File} in filesystem
     *
     * @return database {@link File}
     */
    private static File getDBFile() {
        return new File("./", DB_FILE_NAME);
    }

    /**
     * Connect to database from resources
     */
    private void connect() {
        try {
            // Unpack and connect to database
            dao = new DaoManager(unpackDatabase().toString());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close {@link DaoManager} and delete database {@link File} from filesystem
     */
    @Override
    public void close() {
        Optional.ofNullable(dao).ifPresent(DaoManager::close);
        getDBFile().delete();
    }
}
