package ak.detaysoft.galepress.service_models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by adem on 10/03/14.
 */
public class R_AppCategories {
    private ArrayList<R_Category> categories;

    public R_AppCategories() {
    }

    public R_AppCategories(JSONObject json) {

        this.categories = new ArrayList<R_Category>();
        JSONArray arrayCategories = json.optJSONArray("topics");
        if (null != arrayCategories) {
            int contentsLength = arrayCategories.length();
            for (int i = 0; i < contentsLength; i++) {
                JSONObject item = arrayCategories.optJSONObject(i);
                if (null != item) {
                    this.categories.add(new R_Category(item));
                }
            }
        } else {
            this.categories.clear();
        }
    }

    public ArrayList<R_Category> getCategories() {
        return this.categories;
    }

    public void setContents(ArrayList<R_Category> categories) {
        this.categories = categories;
    }

}
