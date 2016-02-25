package ak.detaysoft.galepress.database_models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;

import ak.detaysoft.galepress.service_models.R_Category;

/**
 * Created by adem on 17/02/14.
 */
@DatabaseTable(tableName = "Category")
public class L_Category implements Serializable { //implement serializable (MG) libraryFragment icinde onsaved metodunda kullanabilmek icin eklendi
    public final static  String ID_FIELD_NAME= "id";

    @DatabaseField(id = true, columnName = ID_FIELD_NAME)
    public Integer categoryID;

    @DatabaseField
    public  String categoryName;

    public L_Category() {
    }

    public L_Category(Integer categoryID, String categoryName) {
        this.categoryID = categoryID;
        this.categoryName = categoryName;
    }

    public L_Category(R_Category remoteCategory) {
        this.categoryID = remoteCategory.getCategoryID();
        this.categoryName =remoteCategory.getCategoryName();
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

    public void updateWithRemoteCategory(R_Category remoteCategory){
        this.categoryName = remoteCategory.getCategoryName();
    }
}
