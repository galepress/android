package ak.detaysoft.galepress.service_models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by adem on 10/03/14.
 */
public class R_AppCategories {
    private Integer status;
    private String error;
    private ArrayList<R_Category> categories;

    public R_AppCategories() {
    }

    public R_AppCategories(JSONObject json) {

        this.status = json.optInt("status");
        this.error = json.optString("error");

        this.categories = new ArrayList<R_Category>();
        JSONArray arrayCategories = json.optJSONArray("Categories");
        if (null != arrayCategories) {
            int contentsLength = arrayCategories.length();
            for (int i = 0; i < contentsLength; i++) {
                JSONObject item = arrayCategories.optJSONObject(i);
                if (null != item) {
                    this.categories.add(new R_Category(item));
                }
            }
        } else {
            JSONObject item = json.optJSONObject("Categories");
            if (null != item) {
                this.categories.add(new R_Category(item));
            }
        }
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public ArrayList<R_Category> getCategories() {
        return this.categories;
    }

    public void setContents(ArrayList<R_Category> categories) {
        this.categories = categories;
    }

    @Override
    public String toString() {
        return "R_AppCategories{" +
                "status=" + status +
                ", error='" + error + '\'' +
                ", categories=" + categories +
                '}';
    }
}
