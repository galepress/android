package ak.detaysoft.galepress.custom_models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by p1025 on 10.09.2015.
 */
public class UserInformations {

    private String accessToken;

    public UserInformations(){

    }

    public UserInformations(String accessToken){
        this.accessToken = accessToken;
    }

    public UserInformations(JSONObject obj) {
        try {
            this.accessToken = obj.getString("accessToken");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("accessToken", accessToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {

        return accessToken;
    }
}
