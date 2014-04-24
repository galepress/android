package ak.detaysoft.galepress.service_models;

import org.json.JSONObject;


public class R_Category {

    private Integer categoryID;
    private String categoryName;


    public R_Category() {

    }

    public R_Category(JSONObject json) {

        this.categoryID = json.optInt("CategoryID");
        this.categoryName = json.optString("CategoryName");

    }

    public Integer getCategoryID() {
        return this.categoryID;
    }

    public void setCategoryID(Integer categoryID) {
        this.categoryID = categoryID;
    }

    public String getCategoryName() {
        return this.categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }



}
