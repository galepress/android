package ak.detaysoft.graff.service_models;

import org.json.JSONObject;


public class R_AppVersion {
	
    private Integer status;
    private boolean applicationBlocked;
    private boolean applicationStatus;
    private Integer applicationVersion;
    private Integer applicationID;
    private String error;
    
    
	public R_AppVersion() {
		
	}	
        
    public R_AppVersion(JSONObject json) {
    
        this.status = json.optInt("status");
        this.applicationBlocked = json.optBoolean("ApplicationBlocked");
        this.applicationStatus = json.optBoolean("ApplicationStatus");
        this.applicationVersion = json.optInt("ApplicationVersion");
        this.applicationID = json.optInt("ApplicationID");
        this.error = json.optString("error");

    }
    
    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public boolean getApplicationBlocked() {
        return this.applicationBlocked;
    }

    public void setApplicationBlocked(boolean applicationBlocked) {
        this.applicationBlocked = applicationBlocked;
    }

    public boolean getApplicationStatus() {
        return this.applicationStatus;
    }

    public void setApplicationStatus(boolean applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Integer getApplicationVersion() {
        return this.applicationVersion;
    }

    public void setApplicationVersion(Integer applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public Integer getApplicationID() {
        return this.applicationID;
    }

    public void setApplicationID(Integer applicationID) {
        this.applicationID = applicationID;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }


    
}
