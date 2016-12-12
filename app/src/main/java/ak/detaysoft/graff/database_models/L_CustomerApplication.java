package ak.detaysoft.graff.database_models;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by p1025 on 09.11.2016.
 */

@DatabaseTable(tableName = "Customer_Application")
public class L_CustomerApplication {

    @DatabaseField (id = true, columnName = "id")
    private String id;

    @DatabaseField
    private String appName;

    @DatabaseField(columnName = "playUrl")
    private String playUrl;

    @DatabaseField
    private Integer version;

    public L_CustomerApplication(){

    }
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getPlayUrl() {
        return playUrl;
    }

    public void setPlayUrl(String playUrl) {
        this.playUrl = playUrl;
    }
}
