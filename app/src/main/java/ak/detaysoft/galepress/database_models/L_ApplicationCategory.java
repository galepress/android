package ak.detaysoft.galepress.database_models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by p1025 on 14.11.2016.
 */

@DatabaseTable(tableName = "Application_Category")
public class L_ApplicationCategory {

    @DatabaseField
    private String coverImageUrl;


    @DatabaseField
    private String order;

    @DatabaseField
    private boolean isUpdated;

    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "application")
    private L_CustomerApplication application;


    @DatabaseField(foreign = true, foreignAutoRefresh = true, columnName = "category")
    private L_Category category;

    public L_ApplicationCategory() {

    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public L_CustomerApplication getApplication() {
        return application;
    }

    public void setApplication(L_CustomerApplication application) {
        this.application = application;
    }

    public L_Category getCategory() {
        return category;
    }

    public void setCategory(L_Category category) {
        this.category = category;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }
}
