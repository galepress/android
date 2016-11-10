package ak.detaysoft.galepress.database_models;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import ak.detaysoft.galepress.custom_models.ApplicationCategory;

/**
 * Created by p1025 on 09.11.2016.
 */

@DatabaseTable(tableName = "Customer_Application")
public class L_CustomerApplication {

    @DatabaseField (id = true, columnName = "id") private String id;
    @DatabaseField private String appName;
    @DatabaseField private String categoryJson;
    @DatabaseField private Integer version;
    @DatabaseField private boolean isUpdated = false;

    private ArrayList<ApplicationCategory> categories;



    public L_CustomerApplication(){

    }

    /*
    * Burasi database insert yapilirken category idler duzenleniyor.
    * */
    public String prepareCategoryIdsJson(){
        JSONArray array = new JSONArray();
        if(categories != null) {
            for(ApplicationCategory item : categories) {
                try {
                    JSONObject object = new JSONObject();
                    object.put("id", item.getId().toString());
                    object.put("coverImageUrl", item.getCoverImageUrl());
                    array.put(object);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        return array.toString();
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

    public String getCategoryJson() {
        return categoryJson;
    }

    public void setCategoryJson(String categoryJson) {
        this.categoryJson = categoryJson;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public ArrayList<ApplicationCategory> getCategories() {
        return categories;
    }

    public void setCategories(ArrayList<ApplicationCategory> categories) {
        this.categories = categories;
    }

    public boolean isUpdated() {
        return isUpdated;
    }

    public void setUpdated(boolean updated) {
        isUpdated = updated;
    }
}
