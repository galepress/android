package ak.detaysoft.graff.database_models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by adem on 26/02/14.
 */
@DatabaseTable(tableName = "Application")
public class L_Application {
    @DatabaseField(id = true) private Integer id;
    @DatabaseField private Integer version;

    public L_Application() {
    }

    public L_Application(Integer id, Integer version) {
        this.id = id;
        this.version = version;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
