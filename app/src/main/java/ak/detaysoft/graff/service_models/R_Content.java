package ak.detaysoft.graff.service_models;

import org.json.JSONObject;


public class R_Content {

    private String contentName;
    private String contentMonthlyName;
    private boolean contentBlocked;
    private Integer contentID;
    private boolean contentStatus;
    private Integer contentVersion;
    private boolean contentIsMaster;
    private Integer contentOrderNo;
    private boolean isForceDelete;
    private boolean contentBought;
    private boolean isBuyable;

    public R_Content() {

    }

    public R_Content(JSONObject json) {

        this.contentName = json.optString("ContentName");
        this.contentMonthlyName = json.optString("ContentMonthlyName");
        this.contentBlocked = json.optBoolean("ContentBlocked");
        this.contentID = json.optInt("ContentID");
        this.contentStatus = json.optBoolean("ContentStatus");
        this.contentVersion = json.optInt("ContentVersion");
        this.contentIsMaster = json.optBoolean("ContentIsMaster");
        this.contentOrderNo = json.optInt("ContentOrderNo");
        this.isForceDelete = json.optBoolean("RemoveFromMobile");
        this.contentBought = json.optBoolean("ContentBought");
        this.isBuyable = json.optBoolean("ContentIsBuyable");
    }

    public String getContentName() {
        return this.contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public String getContentMonthlyName() {
        return this.contentMonthlyName;
    }

    public void setContentMonthlyName(String contentMonthlyName) {
        this.contentMonthlyName = contentMonthlyName;
    }

    public boolean getContentBlocked() {
        return this.contentBlocked;
    }

    public void setContentBlocked(boolean contentBlocked) {
        this.contentBlocked = contentBlocked;
    }

    public Integer getContentID() {
        return this.contentID;
    }

    public void setContentID(Integer contentID) {
        this.contentID = contentID;
    }

    public boolean getContentStatus() {
        return this.contentStatus;
    }

    public void setContentStatus(boolean contentStatus) {
        this.contentStatus = contentStatus;
    }

    public Integer getContentVersion() {
        return this.contentVersion;
    }

    public void setContentVersion(Integer contentVersion) {
        this.contentVersion = contentVersion;
    }

    public boolean isContentIsMaster() {
        return contentIsMaster;
    }

    public void setContentIsMaster(boolean contentIsMaster) {
        this.contentIsMaster = contentIsMaster;
    }

    public void setContentOrderNo(Integer contentOrderNo) {
        this.contentOrderNo = contentOrderNo;
    }

    public Integer getContentOrderNo() {
        return contentOrderNo;
    }

    public boolean isForceDelete() {
        return isForceDelete;
    }

    public void setForceDelete(boolean isForceDelete) {
        this.isForceDelete = isForceDelete;
    }

    public boolean isContentBought() {
        return contentBought;
    }

    public void setContentBought(boolean contentBought) {
        this.contentBought = contentBought;
    }

    public void setIsBuyable(boolean isBuyable) {
        this.isBuyable = isBuyable;
    }

    public boolean isBuyable() {

        return isBuyable;
    }
}
