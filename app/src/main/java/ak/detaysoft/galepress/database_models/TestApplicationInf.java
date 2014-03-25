package ak.detaysoft.galepress.database_models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by adem on 04/03/14.
 */

@DatabaseTable(tableName = "TestApplicationInf")

public class TestApplicationInf {
    @DatabaseField private String username;
    @DatabaseField private String password;
    @DatabaseField private String applicationId;
    @DatabaseField private boolean succeeded;

    public TestApplicationInf() {
    }

    public TestApplicationInf(String username, String password, String applicationId, boolean succeeded) {
        this.username = username;
        this.password = password;
        this.applicationId = applicationId;
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
    }

    public boolean isSucceeded() {
        return succeeded;
    }

    public void setSucceeded(boolean succeeded) {
        this.succeeded = succeeded;
    }
}
