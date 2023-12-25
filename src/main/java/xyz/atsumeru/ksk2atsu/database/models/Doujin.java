package xyz.atsumeru.ksk2atsu.database.models;

import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;

/**
 * Model of {@link Doujin} table in {@link Database} . Corresponds to {@link CatalogType#DOUJIN} value
 */
@EqualsAndHashCode(callSuper = true)
@Data
@DatabaseTable(tableName = "doujin")
public class Doujin extends Content {
}
