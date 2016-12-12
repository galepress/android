package ak.detaysoft.graff.database_models;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import ak.detaysoft.graff.GalePressApplication;

/**
 * Created by adem on 04/03/14.
 */

@DatabaseTable(tableName = "TestApplicationInf")

public class TestApplicationInf {
    @DatabaseField private String username;
    @DatabaseField private String password;
    @DatabaseField private String applicationId;
    @DatabaseField private String facebookEmail;
    @DatabaseField private String facebookUserId;
    @DatabaseField private boolean succeeded;

    public TestApplicationInf() {
    }

    public TestApplicationInf(String username, String password, String applicationId, String facebookEmail, String facebookUserId, boolean succeeded) {
        this.username = username;
        this.password = password;
        this.applicationId = applicationId;
        this.facebookEmail = facebookEmail;
        this.facebookUserId = facebookUserId;
        this.succeeded = succeeded;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
        SharedPreferences preferences;
        SharedPreferences.Editor editor;
        preferences= PreferenceManager.getDefaultSharedPreferences(GalePressApplication.getInstance().getApplicationContext());
        editor = preferences.edit();
        editor.putString("AppId", applicationId);
        editor.commit();
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }

    public String getFacebookEmail() {
        return facebookEmail;
    }

    public String getFacebookUserId() {
        return facebookUserId;
    }

    public void setFacebookEmail(String facebookToken) {
        this.facebookEmail = facebookEmail;
    }

    public void setFacebookUserId(String facebookUserId) {
        this.facebookUserId = facebookUserId;
    }
}
