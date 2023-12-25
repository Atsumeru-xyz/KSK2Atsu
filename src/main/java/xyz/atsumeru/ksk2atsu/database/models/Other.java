package xyz.atsumeru.ksk2atsu.database.models;

import com.j256.ormlite.table.DatabaseTable;
import lombok.Data;
import lombok.EqualsAndHashCode;
import xyz.atsumeru.ksk2atsu.database.Database;
import xyz.atsumeru.ksk2atsu.database.enums.CatalogType;

/**
 * Model of {@link Other} table in {@link Database} . Corresponds to {@link CatalogType#OTHER} value
 */
@EqualsAndHashCode(callSuper = true)
@Data
@DatabaseTable(tableName = "other")
public class Other extends Content {
}
