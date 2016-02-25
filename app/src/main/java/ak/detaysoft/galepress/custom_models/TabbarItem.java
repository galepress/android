package ak.detaysoft.galepress.custom_models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import ak.detaysoft.galepress.GalePressApplication;

/**
 * Created by p1025 on 21.05.2015.
 */
public class TabbarItem implements Serializable {

    private String iconUrl;
    private String webUrl;
    private String title;

    public TabbarItem(){

    }

    public TabbarItem(JSONObject jsonObject){

        try {
            if(GalePressApplication.getInstance().getDeviceDensity() == GalePressApplication.getInstance().XXH_DPI){
                this.iconUrl = jsonObject.getString("tabLogoUrl_3x");
            } else if(GalePressApplication.getInstance().getDeviceDensity() == GalePressApplication.getInstance().XH_DPI){
                this.iconUrl = jsonObject.getString("tabLogoUrl_2x");
            } else if(GalePressApplication.getInstance().getDeviceDensity() == GalePressApplication.getInstance().H_DPI){
                this.iconUrl = jsonObject.getString("tabLogoUrl_1x");
            } else {
                this.iconUrl = jsonObject.getString("tabLogoUrl");
            }

            this.webUrl = jsonObject.getString("tabUrl");
            this.title = jsonObject.getString("tabTitle");
        } catch (JSONException e) {
            this.iconUrl = "";
            this.webUrl = "";
            this.title = "";
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return "Tab{" +
                "iconUrl='"+iconUrl+'\''+
                ", webUrl='"+webUrl+'\''+
                ", title='"+title+'\''+
                "}";
    }
}
