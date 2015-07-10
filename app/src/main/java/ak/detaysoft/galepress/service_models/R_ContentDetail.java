package ak.detaysoft.galepress.service_models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;


public class R_ContentDetail {

    private String contentMonthlyName;
    private Integer contentID;
    private boolean contentBlocked;
    private double contentCurrency;
    private ArrayList<R_Category> contentCategories;
    private String contentDetail;
    private Integer contentCategoryID;
    private String contentIdentifier;
    private boolean contentIsProtected;
    private String contentPrice;
    private String error;
    private boolean contentAutoDownload;
    private String contentName;
    private boolean contentStatus;
    private boolean contentIsBuyable;
    private Integer contentVersion;
    private Integer contentPdfVersion;
    private Integer contentCoverImageVersion;
    private String contentCategoryName;
    private Integer status;
    private boolean contentIsMaster;
    private Integer contentOrientation;
    private Integer contentOrderNo;


    public R_ContentDetail() {

    }

    public R_ContentDetail(JSONObject json) {

        this.contentMonthlyName = json.optString("ContentMonthlyName");
        this.contentID = json.optInt("ContentID");
        this.contentBlocked = json.optBoolean("ContentBlocked");
        this.contentCurrency = json.optDouble("ContentCurrency");
        this.contentDetail = json.optString("ContentDetail");
        this.contentCategoryID = json.optInt("ContentCategoryID");
        this.contentIdentifier = json.optString("ContentIdentifier");
        this.contentIsProtected = json.optBoolean("ContentIsProtected");
        this.contentPrice = json.optString("ContentPrice");
        this.error = json.optString("error");
        this.contentAutoDownload = json.optBoolean("ContentAutoDownload");
        this.contentName = json.optString("ContentName");
        this.contentStatus = json.optBoolean("ContentStatus");
        this.contentIsBuyable = json.optBoolean("ContentIsBuyable");
        this.contentVersion = json.optInt("ContentVersion");
        this.contentPdfVersion = json.optInt("ContentPdfVersion");
        this.contentCoverImageVersion = json.optInt("ContentCoverImageVersion");
        this.contentCategoryName = json.optString("ContentCategoryName");
        this.status = json.optInt("status");
        this.contentIsMaster = json.optBoolean("ContentIsMaster");
        this.contentOrientation = json.optInt("ContentOrientation");
        this.contentOrderNo = json.optInt("ContentOrderNo");
        this.contentCategories = new ArrayList<R_Category>();


        JSONArray arrayContentCategories = json.optJSONArray("ContentCategories");
        if (null != arrayContentCategories) {
            int contentCategoriesLength = arrayContentCategories.length();
            for (int i = 0; i < contentCategoriesLength; i++) {
                JSONObject item = arrayContentCategories.optJSONObject(i);
                if (null != item) {
                    this.contentCategories.add(new R_Category(item));
                }
            }
        }
        else {
            JSONObject item = json.optJSONObject("R_Category");
            if (null != item) {
                this.contentCategories.add(new R_Category(item));
            }
        }

    }

    public String getContentMonthlyName() {
        return this.contentMonthlyName;
    }

    public void setContentMonthlyName(String contentMonthlyName) {
        this.contentMonthlyName = contentMonthlyName;
    }

    public Integer getContentID() {
        return this.contentID;
    }

    public void setContentID(Integer contentID) {
        this.contentID = contentID;
    }

    public boolean getContentBlocked() {
        return this.contentBlocked;
    }

    public void setContentBlocked(boolean contentBlocked) {
        this.contentBlocked = contentBlocked;
    }

    public double getContentCurrency() {
        return this.contentCurrency;
    }

    public void setContentCurrency(double contentCurrency) {
        this.contentCurrency = contentCurrency;
    }

    public String getContentDetail() {
        return this.contentDetail;
    }

    public void setContentDetail(String contentDetail) {
        this.contentDetail = contentDetail;
    }

    public Integer getContentCategoryID() {
        return this.contentCategoryID;
    }

    public void setContentCategoryID(Integer contentCategoryID) {
        this.contentCategoryID = contentCategoryID;
    }

    public String getContentIdentifier() {
        return this.contentIdentifier;
    }

    public void setContentIdentifier(String contentIdentifier) {
        this.contentIdentifier = contentIdentifier;
    }

    public boolean getContentIsProtected() {
        return this.contentIsProtected;
    }

    public void setContentIsProtected(boolean contentIsProtected) {
        this.contentIsProtected = contentIsProtected;
    }

    public String getContentPrice() {
        return this.contentPrice;
    }

    public void setContentPrice(String contentPrice) {
        this.contentPrice = contentPrice;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public boolean getContentAutoDownload() {
        return this.contentAutoDownload;
    }

    public void setContentAutoDownload(boolean contentAutoDownload) {
        this.contentAutoDownload = contentAutoDownload;
    }

    public String getContentName() {
        return this.contentName;
    }

    public void setContentName(String contentName) {
        this.contentName = contentName;
    }

    public boolean getContentStatus() {
        return this.contentStatus;
    }

    public void setContentStatus(boolean contentStatus) {
        this.contentStatus = contentStatus;
    }

    public boolean getContentIsBuyable() {
        return this.contentIsBuyable;
    }

    public void setContentIsBuyable(boolean contentIsBuyable) {
        this.contentIsBuyable = contentIsBuyable;
    }

    public Integer getContentVersion() {
        return this.contentVersion;
    }

    public void setContentVersion(Integer contentVersion) {
        this.contentVersion = contentVersion;
    }

    public Integer getContentPdfVersion() {
        return this.contentPdfVersion;
    }

    public void setContentPdfVersion(Integer contentPdfVersion) {
        this.contentPdfVersion = contentPdfVersion;
    }

    public Integer getContentCoverImageVersion() {
        return this.contentCoverImageVersion;
    }

    public void setContentCoverImageVersion(Integer contentCoverImageVersion) {
        this.contentCoverImageVersion = contentCoverImageVersion;
    }

    public String getContentCategoryName() {
        return this.contentCategoryName;
    }

    public void setContentCategoryName(String contentCategoryName) {
        this.contentCategoryName = contentCategoryName;
    }

    public Integer getStatus() {
        return this.status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public ArrayList<R_Category> getContentCategories() {
        return this.contentCategories;
    }

    public void setContentCategories(ArrayList<R_Category> contentCategories) {
        this.contentCategories = contentCategories;
    }

    public boolean isContentIsMaster() {
        return contentIsMaster;
    }

    public void setContentIsMaster(boolean contentIsMaster) {
        this.contentIsMaster = contentIsMaster;
    }

    public Integer getContentOrientation() {
        return contentOrientation;
    }

    public void setContentOrientation(Integer contentOrientation) {
        this.contentOrientation = contentOrientation;
    }

    public Integer getContentOrderNo() {
        return contentOrderNo;
    }

    public void setContentOrderNo(Integer contentOrderNo) {
        this.contentOrderNo = contentOrderNo;
    }
}
