package xyz.atsumeru.ksk2atsu.database.models;

import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;

/**
 * Model of {@link Book} table in {@link Database} . Corresponds to {@link CatalogType#BOOKS} value
 */
@EqualsAndHashCode(callSuper = true)
@Data
@DatabaseTable(tableName = "book")
public class Book extends Content {
}
