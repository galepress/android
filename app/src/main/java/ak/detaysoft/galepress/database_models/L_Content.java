package ak.detaysoft.galepress.database_models;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import ak.detaysoft.galepress.GalePressApplication;
import ak.detaysoft.galepress.service_models.R_ContentDetail;

/**
 * Created by adem on 13/02/14.
 */
@DatabaseTable(tableName = "Content")
public class L_Content implements Serializable {
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
    @DatabaseField private String bigCoverImageFileName;
    @DatabaseField private boolean isBuyable;
    @DatabaseField private boolean isProtected;
    @DatabaseField private boolean isMaster;
    @DatabaseField private String password;
    @DatabaseField private double currency;
    @DatabaseField private String price;
    @DatabaseField private Integer coverImageVersion;
    @DatabaseField private Integer remoteCoverImageVersion;
    @DatabaseField private Integer remoteLargeCoverImageVersion;
    @DatabaseField private Integer status;
    @DatabaseField private Integer version;
    @DatabaseField private Integer contentOrientation;
    @DatabaseField private Integer contentOrderNo;
    @DatabaseField private String largeCoverImageDownloadPath;
    @DatabaseField private String smallCoverImageDownloadPath;
    @DatabaseField private boolean isForceDetele;
    @DatabaseField private String identifier;
    @DatabaseField private boolean isOwnedProduct;
    @DatabaseField private String marketPrice;
    @DatabaseField private boolean contentBought;

    private ArrayList<L_Category> categories;
    private String pdfPath;
    private String gridThumbCoverImagePath;
    private String bigCoverImagePath;



    public L_Content(){

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

    public Integer getRemoteCoverImageVersion() {
        return remoteCoverImageVersion;
    }

    public void setCoverImageRemoteVersion(Integer remoteCoverImageVersion) {
        this.remoteCoverImageVersion = remoteCoverImageVersion;
    }

    public Integer getRemoteLargeCoverImageVersion() {
        return remoteLargeCoverImageVersion;
    }

    public void setRemoteLargeCoverImageVersion(Integer remoteLargeCoverImageVersion) {
        this.remoteLargeCoverImageVersion = remoteLargeCoverImageVersion;
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

    public String getPdfFilePath(){
        return this.getPdfPath()+"/file.pdf";
    }

    public boolean isMaster() {
        return isMaster;
    }

    public void setMaster(boolean isMaster) {
        this.isMaster = isMaster;
    }

    public Integer getContentOrientation() {
        return contentOrientation;
    }

    public void setContentOrientation(Integer contentOrientation) {
        this.contentOrientation = contentOrientation;
    }

    public String getBigCoverImageFileName() {
        return bigCoverImageFileName;
    }

    public void setBigCoverImagePath(String bigCoverImagePath) {
        this.bigCoverImagePath = bigCoverImagePath;
    }

    public Integer getContentOrderNo() {
        return contentOrderNo;
    }

    public void setContentOrderNo(Integer contentOrderNo) {
        this.contentOrderNo = contentOrderNo;
    }

    public String getLargeCoverImageDownloadPath() {
        return largeCoverImageDownloadPath;
    }

    public String getSmallCoverImageDownloadPath() {
        return smallCoverImageDownloadPath;
    }

    public void setLargeCoverImageDownloadPath(String largeCoverImageDownloadPath) {
        this.largeCoverImageDownloadPath = largeCoverImageDownloadPath;
    }

    public void setSmallCoverImageDownloadPath(String smallCoverImageDownloadPath) {
        this.smallCoverImageDownloadPath = smallCoverImageDownloadPath;
    }

    public boolean isForceDetele() {
        return isForceDetele;
    }

    public void setForceDetele(boolean isForceDetele) {
        this.isForceDetele = isForceDetele;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isOwnedProduct() {
        return isOwnedProduct;
    }

    public void setOwnedProduct(boolean isOwnedProduct) {
        this.isOwnedProduct = isOwnedProduct;
    }

    public String getMarketPrice() {
        return marketPrice;
    }

    public boolean isContentBought() {
        return contentBought;
    }

    public void setContentBought(boolean contentBought) {
        this.contentBought = contentBought;
    }

    public void setMarketPrice(String marketPrice) {
        this.marketPrice = marketPrice;
    }
// Model Methods

    public L_Content(R_ContentDetail remoteContent){
        this.id = remoteContent.getContentID();
        this.pdfFileName = UUID.randomUUID().toString();
        this.dirName = UUID.randomUUID().toString();
        this.coverImageFileName = UUID.randomUUID().toString();
        this.bigCoverImageFileName = UUID.randomUUID().toString();
        this.coverImageVersion  = -1;
        this.pdfVersion = -1;
        this.version = -1;
        this.remoteCoverImageVersion = -1;
        this.remoteLargeCoverImageVersion = -1;
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
        this.isMaster = remoteContent.isContentIsMaster();
        this.contentOrientation = remoteContent.getContentOrientation();
        this.contentOrderNo = remoteContent.getContentOrderNo();
        this.isForceDetele = remoteContent.isForceDelete();
        this.identifier = remoteContent.getContentIdentifier();
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
        this.isMaster = remoteContent.isContentIsMaster();
        this.contentOrientation = remoteContent.getContentOrientation();
        this.contentOrderNo = remoteContent.getContentOrderNo();
        this.isForceDetele = remoteContent.isForceDelete();
        this.identifier = remoteContent.getContentIdentifier();
    }

    public void updateWithImageDownloadUrl(String url, boolean isLargeCover){
        if(isLargeCover)
            this.largeCoverImageDownloadPath = url;
        else
            this.smallCoverImageDownloadPath = url;
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
                ", bigCoverImageFileName='" + bigCoverImageFileName + '\'' +
                ", isBuyable=" + isBuyable +
                ", isProtected=" + isProtected +
                ", password='" + password + '\'' +
                ", currency=" + currency +
                ", price='" + price + '\'' +
                ", coverImageVersion=" + coverImageVersion +
                ", status=" + status +
                ", version=" + version +
                ", remoteCoverImageVersion=" + remoteCoverImageVersion +
                ", remoteLargeCoverImageVersion=" + remoteLargeCoverImageVersion +
                ", categories=" + categories +
                ", pdfPath='" + pdfPath + '\'' +
                ", gridThumbCoverImagePath='" + gridThumbCoverImagePath + '\'' +
                ", bigCoverImagePath='" + bigCoverImagePath + '\'' +
                ", isMaster='" + isMaster + '\'' +
                ", contentOrientation='"+contentOrientation+'\''+
                ", contentOrderNo='"+contentOrderNo+'\''+
                ", largeCoverImageDownloadPath='"+largeCoverImageDownloadPath+'\''+
                ", smallCoverImageDownloadPath='"+smallCoverImageDownloadPath+'\''+
                ", isForceDetele=" + isForceDetele +
                ", contentBought=" + contentBought +
                ", identifier='"+identifier+'\''+
                ", isOwnedProduct=" + isOwnedProduct +
                ", marketPrice='"+marketPrice+'\''+
                '}';
    }
}
