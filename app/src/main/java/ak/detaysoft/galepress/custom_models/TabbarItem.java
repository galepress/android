package ak.detaysoft.galepress.custom_models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by p1025 on 21.05.2015.
 */
public class TabbarItem implements Serializable {

    private String iconUrl;
    private String webUrl;

    public TabbarItem(){

    }

    public TabbarItem(JSONObject jsonObject){

        try {
            this.iconUrl = jsonObject.getString("tabLogoUrl");
            this.webUrl = jsonObject.getString("tabUrl");
        } catch (JSONException e) {
            this.iconUrl = "";
            this.webUrl = "";
            e.printStackTrace();
        }

    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getWebUrl() {
        return webUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    public void setWebUrl(String webUrl) {
        this.webUrl = webUrl;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "iconUrl='"+iconUrl+'\''+
                ", webUrl='"+webUrl+'\''+
                "}";
    }
}
