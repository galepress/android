package ak.detaysoft.graff.service_models;

/**
 * Created by adem on 29/10/14.
 */

import org.json.JSONObject;



public class R_AppDetail {

    public static Integer FORCE_DO_NOTHING = 0;
    public static Integer FORCE_WARN = 1;
    public static Integer FORCE_BLOCK_APP = 2;
    public static Integer FORCE_BLOCK_AND_DELETE= 3;

    private boolean status;
    private Integer customerId;
    private String customerName;
    private Integer applicationId;
    private String applicationName;
    private String applicationDetail;
    private String applicationExpirationDate;
    private Integer iOSVersion;
    private String iOSLink;
    private Integer androidVersion;
    private String androidLink;
    private boolean applicationBlocked;
    private boolean applicationStatus;
    private Integer applicationVersion;
    private Integer force;

    public R_AppDetail() {

    }

    public R_AppDetail(JSONObject json) {
        this.status = json.optBoolean("status");
        this.customerId = json.optInt("CustomerID");
        this.customerName = json.optString("CustomerName");
        this.applicationId = json.optInt("ApplicationID");
        this.applicationName = json.optString("ApplicationName");
        this.applicationDetail = json.optString("ApplicationDetail");
        this.applicationExpirationDate = json.optString("ApplicationExpirationDate");
        this.iOSVersion = json.optInt("IOSVersion");
        this.iOSLink = json.optString("IOSLink");
        this.androidVersion = json.optInt("AndroidVersion");
        this.androidLink = json.optString("AndroidLink");
        this.applicationBlocked = json.optBoolean("ApplicationBlocked");
        this.applicationStatus = json.optBoolean("ApplicationStatus");
        this.applicationVersion = json.optInt("ApplicationVersion");
        this.force = json.optInt("Force");
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public Integer getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(Integer applicationId) {
        this.applicationId = applicationId;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationDetail() {
        return applicationDetail;
    }

    public void setApplicationDetail(String applicationDetail) {
        this.applicationDetail = applicationDetail;
    }

    public String getApplicationExpirationDate() {
        return applicationExpirationDate;
    }

    public void setApplicationExpirationDate(String applicationExpirationDate) {
        this.applicationExpirationDate = applicationExpirationDate;
    }

    public Integer getiOSVersion() {
        return iOSVersion;
    }

    public void setiOSVersion(Integer iOSVersion) {
        this.iOSVersion = iOSVersion;
    }

    public String getiOSLink() {
        return iOSLink;
    }

    public void setiOSLink(String iOSLink) {
        this.iOSLink = iOSLink;
    }

    public Integer getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(Integer androidVersion) {
        this.androidVersion = androidVersion;
    }

    public String getAndroidLink() {
        return androidLink;
    }

    public void setAndroidLink(String androidLink) {
        this.androidLink = androidLink;
    }

    public boolean isApplicationBlocked() {
        return applicationBlocked;
    }

    public void setApplicationBlocked(boolean applicationBlocked) {
        this.applicationBlocked = applicationBlocked;
    }

    public boolean isApplicationStatus() {
        return applicationStatus;
    }

    public void setApplicationStatus(boolean applicationStatus) {
        this.applicationStatus = applicationStatus;
    }

    public Integer getApplicationVersion() {
        return applicationVersion;
    }

    public void setApplicationVersion(Integer applicationVersion) {
        this.applicationVersion = applicationVersion;
    }

    public Integer getForce() {
        return force;
    }

    public void setForce(Integer force) {
        this.force = force;
    }
}
