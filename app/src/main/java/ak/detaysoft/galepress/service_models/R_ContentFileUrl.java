package ak.detaysoft.galepress.service_models;

import org.json.JSONObject;

/**
 * Created by adem on 26/02/14.
 */
public class R_ContentFileUrl {


        private Integer status;
        private String error;
        private Integer contentID;
        private String url;

    public R_ContentFileUrl(Integer status, String error, Integer contentID, String url) {
        this.status = status;
        this.error = error;
        this.contentID = contentID;
        this.url = url;
    }

    public R_ContentFileUrl(JSONObject json){
        this.status = json.optInt("status");
        this.error = json.optString("error");
        this.contentID = json.optInt("ContentID");
        this.url = json.optString("Url");
    }

    public R_ContentFileUrl() {
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getContentID() {
        return contentID;
    }

    public void setContentID(Integer contentID) {
        this.contentID = contentID;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
