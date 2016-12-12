package ak.detaysoft.graff.custom_models;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by p1025 on 10.09.2015.
 */
public class UserInformations {

    private String accessToken;
    private String userName;

    public UserInformations(){

    }

    public UserInformations(String accessToken, String userName){
        this.accessToken = accessToken;
        this.userName = userName;
    }

    public UserInformations(JSONObject obj) {
        try {
            this.accessToken = obj.getString("accessToken");
            this.userName = "GÜNEŞ"; //TODO obj.getString("userName");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public JSONObject getJSONObject() {
        JSONObject obj = new JSONObject();
        try {
            obj.put("accessToken", accessToken);
            obj.put("userName", userName);
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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
