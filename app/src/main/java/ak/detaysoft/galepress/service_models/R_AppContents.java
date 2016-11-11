package ak.detaysoft.galepress.service_models;

/**
 * Created by adem on 13/02/14.
 */

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class R_AppContents {

    private Integer status;
    private String error;
    private ArrayList<R_Content> contents;


    public R_AppContents() {

    }

    public R_AppContents(JSONObject json) {

        this.status = json.optInt("status");
        this.error = json.optString("error");

        this.contents = new ArrayList<R_Content>();
        JSONArray arrayContents = json.optJSONArray("Contents");
        if (null != arrayContents) {
            int contentsLength = arrayContents.length();
            for (int i = 0; i < contentsLength; i++) {
                JSONObject item = arrayContents.optJSONObject(i);
                if (null != item) {
                    this.contents.add(new R_Content(item));
                }
            }
        } else {
            JSONObject item = json.optJSONObject("Contents");
            if (null != item) {
                this.contents.add(new R_Content(item));
            }
        }


    }

    public R_AppContents(JSONArray json) {

        this.contents = new ArrayList<R_Content>();
        if (null != json) {
            int contentsLength = json.length();
            for (int i = 0; i < contentsLength; i++) {
                JSONObject item = json.optJSONObject(i);
                if (null != item) {
                    this.contents.add(new R_Content(item));
                }
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

    public ArrayList<R_Content> getContents() {
        return this.contents;
    }

    public void setContents(ArrayList<R_Content> contents) {
        this.contents = contents;
    }


}
