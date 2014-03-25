package ak.detaysoft.galepress.database_models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by adem on 26/02/14.
 */
@DatabaseTable(tableName = "Content_Category")
public class L_ContentCategory {
    public final static String CONTENT_ID_FIELD_NAME = "content_id";
    public final static String CATEGORY_ID_FIELD_NAME = "category_id";
    @DatabaseField(generatedId = true) private Integer id;
    @DatabaseField(foreign = true, columnName = CATEGORY_ID_FIELD_NAME) L_Category category;
    @DatabaseField(foreign = true, columnName = CONTENT_ID_FIELD_NAME) L_Content content;

    public L_ContentCategory() {
    }

    public L_ContentCategory(L_Category category, L_Content content) {
        this.category = category;
        this.content = content;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public L_Category getCategory() {
        return category;
    }

    public void setCategory(L_Category category) {
        this.category = category;
    }

    public L_Content getContent() {
        return content;
    }

    public void setContent(L_Content content) {
        this.content = content;
    }
}
