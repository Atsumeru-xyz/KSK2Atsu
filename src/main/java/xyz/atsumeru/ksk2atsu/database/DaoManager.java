package xyz.atsumeru.ksk2atsu.database;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;
import xyz.atsumeru.ksk2atsu.database.models.Book;
import xyz.atsumeru.ksk2atsu.database.models.Content;
import xyz.atsumeru.ksk2atsu.database.models.Doujin;
import xyz.atsumeru.ksk2atsu.database.models.Other;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Dao manager for metadata dump database. Supports Unlimited, Doujin, Books and Other metadata tables
 */
public class DaoManager {
    private final ConnectionSource connectionSource;

    private final Dao<Content, String> contentDao;
    private final Dao<Doujin, String> doujinDao;
    private final Dao<Book, String> bookDao;
    private final Dao<Other, String> otherDao;

    /**
     * Create Dao manager, connect to database by path and create Dao for tables
     *
     * @param dbPath database file path (*.db)
     * @throws SQLException
     */
    public DaoManager(String dbPath) throws SQLException {
        // Connect to database
        connectionSource = new JdbcConnectionSource("jdbc:sqlite:" + dbPath);

        // Create Dao's
        contentDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, Content.class);
        doujinDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, Doujin.class);
        bookDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, Book.class);
        otherDao = com.j256.ormlite.dao.DaoManager.createDao(connectionSource, Other.class);

        // Create tables if not exists
        TableUtils.createTableIfNotExists(connectionSource, Content.class);
        TableUtils.createTableIfNotExists(connectionSource, Doujin.class);
        TableUtils.createTableIfNotExists(connectionSource, Book.class);
        TableUtils.createTableIfNotExists(connectionSource, Other.class);
    }

    /**
     * Query all {@link Content} from table by {@link CatalogType}
     *
     * @param catalogType {@link CatalogType} that will be used to select correct Dao
     * @return list of {@link Content} from table or empty {@link List} if {@link SQLException} occur(normally should not happen)
     */
    public List<? extends Content> queryAll(CatalogType catalogType) {
        try {
            return switch (catalogType) {
                case UNLIMITED -> contentDao.queryForAll();
                case DOUJIN -> doujinDao.queryForAll();
                case BOOKS -> bookDao.queryForAll();
                case OTHER -> otherDao.queryForAll();
            };
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    /**
     * Close open database connection
     */
    public void close() {
        try {
            this.connectionSource.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
