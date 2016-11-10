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
    public Integer id;

    @DatabaseField
    public  String name;

    public L_Category() {
    }

    public L_Category(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public L_Category(R_Category remoteCategory) {
        this.id = remoteCategory.getId();
        this.name =remoteCategory.getName();
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updateWithRemoteCategory(R_Category remoteCategory){
        this.name = remoteCategory.getName();
    }
}
