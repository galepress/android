package ak.detaysoft.galepress.database_models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.UUID;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.service_models.R_Category;
import ak.detaysoft.galepress.service_models.R_ContentDetail;

/**
 * Created by adem on 13/02/14.
 */
@DatabaseTable(tableName = "Content")
public class L_Content {
    public final static  String ID_FIELD_NAME= "id";
    public final static  String NAME_FIELD_NAME= "name";
    public final static  String IS_PDF_DOWNLOADED_FIELD_NAME= "isPdfDownloaded";

    @DatabaseField(id = true, columnName = ID_FIELD_NAME) private Integer id;
    @DatabaseField(columnName = NAME_FIELD_NAME) private String name;
    @DatabaseField private boolean autoDownload;
    @DatabaseField private boolean blocked;
    @DatabaseField private Integer pdfVersion;
    @DatabaseField private String monthlyName;
    @DatabaseField private String detail;
    @DatabaseField private String pdfFileName;
    @DatabaseField private String dirName;
    @DatabaseField private boolean isPdfUpdateAvailable;
    @DatabaseField(columnName = IS_PDF_DOWNLOADED_FIELD_NAME) private boolean isPdfDownloaded;
    @DatabaseField private boolean isPdfDownloading;
    @DatabaseField private boolean isCoverImageUpdateAvailable;
    @DatabaseField private String coverImageFileName;
    @DatabaseField private boolean isBuyable;
    @DatabaseField private boolean isProtected;
    @DatabaseField private String password;
    @DatabaseField private double currency;
    @DatabaseField private String price;
    @DatabaseField private Integer coverImageVersion;
    @DatabaseField private Integer status;
    @DatabaseField private Integer version;

    private ArrayList<L_Category> categories;
    private String pdfPath;
    private String gridThumbCoverImagePath;
    private String bigCoverImagePath;


    public L_Content(){

    }

    public L_Content(
            Integer id,
            String name,
            boolean autoDownload,
            boolean blocked,
            Integer pdfVersion,
            String monthlyName,
            String detail,
            String pdfFileName,
            String dirName,
            boolean isPdfUpdateAvailable,
            boolean isPdfDownloaded,
            String coverImageFileName,
            boolean isBuyable,
            boolean isProtected,
            String password,
            double currency,
            String price,
            Integer coverImageVersion,
            Integer status,
            Integer version,
            ArrayList<L_Category> categories) {
        this.blocked = blocked;
        this.autoDownload = autoDownload;
        this.pdfVersion = pdfVersion;
        this.monthlyName = monthlyName;
        this.detail = detail;
        this.pdfFileName = pdfFileName;
        this.dirName = dirName;
        this.isPdfUpdateAvailable = isPdfUpdateAvailable;
        this.isPdfDownloaded = isPdfDownloaded;
        this.coverImageFileName = coverImageFileName;
        this.id = id;
        this.isBuyable = isBuyable;
        this.isProtected = isProtected;
        this.name = name;
        this.password = password;
        this.currency = currency;
        this.price = price;
        this.coverImageVersion = coverImageVersion;
        this.status = status;
        this.version = version;
        this.categories = categories;
        this.isPdfDownloading = false;
    }

    public boolean isCoverImageUpdateAvailable() {
        return isCoverImageUpdateAvailable;
    }

    public void setCoverImageUpdateAvailable(boolean isCoverImageUpdateAvailable) {
        this.isCoverImageUpdateAvailable = isCoverImageUpdateAvailable;
    }

    public boolean isAutoDownload() {
        return autoDownload;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public Integer getPdfVersion() {
        return pdfVersion;
    }

    public String getMonthlyName() {
        return monthlyName;
    }

    public String getDetail() {
        return detail;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public String getDirName() {
        return dirName;
    }

    public boolean isPdfUpdateAvailable() {
        return isPdfUpdateAvailable;
    }

    public boolean isPdfDownloaded() {
        return isPdfDownloaded;
    }

    public String getCoverImageFileName() {
        return coverImageFileName;
    }

    public Integer getId() {
        return id;
    }

    public boolean isBuyable() {
        return isBuyable;
    }

    public boolean isProtected() {
        return isProtected;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public double getCurrency() {
        return currency;
    }

    public String getPrice() {
        return price;
    }

    public Integer getCoverImageVersion() {
        return coverImageVersion;
    }

    public Integer getStatus() {
        return status;
    }

    public Integer getVersion() {
        return version;
    }

    public ArrayList<L_Category> getCategories() {
        return categories;
    }

    public String getPdfPath() {
        return GalePressApplication.getInstance().getFilesDir()+"/"+getId().toString();
    }

    public String getGridThumbCoverImagePath() {
        return gridThumbCoverImagePath;
    }

    public String getBigCoverImagePath() {
        return bigCoverImagePath;
    }

    public void setAutoDownload(boolean autoDownload) {
        this.autoDownload = autoDownload;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public void setPdfVersion(Integer pdfVersion) {
        this.pdfVersion = pdfVersion;
    }

    public void setMonthlyName(String monthlyName) {
        this.monthlyName = monthlyName;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public void setPdfUpdateAvailable(boolean isPdfUpdateAvailable) {
        this.isPdfUpdateAvailable = isPdfUpdateAvailable;
    }

    public void setPdfDownloaded(boolean isPdfDownloaded) {
        this.isPdfDownloaded = isPdfDownloaded;
    }

    public void setCoverImageFileName(String coverImageFileName) {
        this.coverImageFileName = coverImageFileName;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setBuyable(boolean isBuyable) {
        this.isBuyable = isBuyable;
    }

    public void setProtected(boolean isProtected) {
        this.isProtected = isProtected;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCurrency(double currency) {
        this.currency = currency;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setCoverImageVersion(Integer coverImageVersion) {
        this.coverImageVersion = coverImageVersion;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setCategories(ArrayList<L_Category> categories) {
        this.categories = categories;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }

    public boolean isPdfDownloading() { return isPdfDownloading; }

    public void setPdfDownloading(boolean isPdfDownloading) { this.isPdfDownloading = isPdfDownloading;}

    // Model Methods

    public L_Content(R_ContentDetail remoteContent){
        this.id = remoteContent.getContentID();
        this.pdfFileName = UUID.randomUUID().toString();
        this.dirName = UUID.randomUUID().toString();
        this.coverImageFileName = UUID.randomUUID().toString();
        this.coverImageVersion  = -1;
        this.pdfVersion = -1;
        this.version = -1;
        this.isPdfDownloaded = false;
        /*
        for (R_Category remoteCategory : remoteContent.getContentCategories()){
            L_Category localCategory = GalePressApplication.getInstance().getDatabaseApi().getCategory(remoteCategory.getCategoryID());
            if(localCategory == null){
                localCategory = new L_Category(remoteCategory);
            }
            this.categories.add(localCategory);
        }
        */
        this.blocked = remoteContent.getContentBlocked();
        this.autoDownload = remoteContent.getContentAutoDownload();
        this.monthlyName = remoteContent.getContentMonthlyName();
        this.detail = remoteContent.getContentDetail();
        this.isPdfUpdateAvailable = false;
        this.isPdfDownloaded = false;
        this.isBuyable = remoteContent.getContentIsBuyable();
        this.isProtected = remoteContent.getContentIsProtected();
        this.name = remoteContent.getContentName();
        this.currency = remoteContent.getContentCurrency();
        this.price = remoteContent.getContentPrice();
        this.status = remoteContent.getStatus();
    }

    public void updateWithRemoteContent(R_ContentDetail remoteContent){

        /*
        for (R_Category remoteCategory : remoteContent.getContentCategories()){
            L_Category localCategory = GalePressApplication.getInstance().getDatabaseApi().getCategory(remoteCategory.getCategoryID());
            if(localCategory == null){
                localCategory = new L_Category(remoteCategory);
                GalePressApplication.getInstance().getDatabaseApi().setCategory(localCategory);
            }
            this.categories.add(localCategory);
        }
        */
        this.blocked = remoteContent.getContentBlocked();
        this.autoDownload = remoteContent.getContentAutoDownload();
        this.monthlyName = remoteContent.getContentMonthlyName();
        this.detail = remoteContent.getContentDetail();
        this.isBuyable = remoteContent.getContentIsBuyable();
        this.isProtected = remoteContent.getContentIsProtected();
        this.name = remoteContent.getContentName();
        this.currency = remoteContent.getContentCurrency();
        this.price = remoteContent.getContentPrice();
        this.status = remoteContent.getStatus();
    }

    @Override
    public String toString() {
        return "L_Content{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", autoDownload=" + autoDownload +
                ", blocked=" + blocked +
                ", pdfVersion=" + pdfVersion +
                ", monthlyName='" + monthlyName + '\'' +
                ", detail='" + detail + '\'' +
                ", pdfFileName='" + pdfFileName + '\'' +
                ", dirName='" + dirName + '\'' +
                ", isPdfUpdateAvailable=" + isPdfUpdateAvailable +
                ", isPdfDownloaded=" + isPdfDownloaded +
                ", isCoverImageUpdateAvailable=" + isCoverImageUpdateAvailable +
                ", coverImageFileName='" + coverImageFileName + '\'' +
                ", isBuyable=" + isBuyable +
                ", isProtected=" + isProtected +
                ", password='" + password + '\'' +
                ", currency=" + currency +
                ", price='" + price + '\'' +
                ", coverImageVersion=" + coverImageVersion +
                ", status=" + status +
                ", version=" + version +
                ", categories=" + categories +
                ", pdfPath='" + pdfPath + '\'' +
                ", gridThumbCoverImagePath='" + gridThumbCoverImagePath + '\'' +
                ", bigCoverImagePath='" + bigCoverImagePath + '\'' +
                '}';
    }
}
