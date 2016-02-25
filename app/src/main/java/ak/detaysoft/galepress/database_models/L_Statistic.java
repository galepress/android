package ak.detaysoft.galepress.database_models;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by adem on 18/02/14.
 */
@DatabaseTable(tableName = "Statistic")
public class L_Statistic {

    @DatabaseField (id = true, columnName = "id") private String id;
    @DatabaseField private Integer contentId;
    @DatabaseField private Double lat;
    @DatabaseField private Double lon;
    @DatabaseField private Integer page;
    @DatabaseField private String time;
    @DatabaseField private Integer type;
    @DatabaseField private String param5;
    @DatabaseField private String param6;
    @DatabaseField private String param7;

    public final static int STATISTIC_applicationActive = 1;
    public final static int STATISTIC_applicationPassive = 2;
    public final static int STATISTIC_applicationTerminated = 3;
    public final static int STATISTIC_contentDownloaded = 10;
    public final static int STATISTIC_contentUpdated = 11;
    public final static int STATISTIC_contentOpened = 12;
    public final static int STATISTIC_contentClosed = 13;
    public final static int STATISTIC_contentDeleted = 14;
    public final static int STATISTIC_pageOpenedPortrait = 21;
    public final static int STATISTIC_pageOpenedLandscape = 22;

    public L_Statistic() {
        this.param5 = "android";
    }

    public L_Statistic(String id, Integer contentId, Double lat, Double lon, Integer page, String time, Integer type, String param5, String param6, String param7) {
        this.id = id;
        this.contentId = contentId;
        this.lat = lat;
        this.lon = lon;
        this.page = page;
        this.time = time;
        this.type = type;
        this.param5 = "android";
        this.param6 = param6;
        this.param7 = param7;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLon() {
        return lon;
    }

    public void setLon(Double lon) {
        this.lon = lon;
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getParam5() {
        return param5;
    }

    public void setParam5(String param5) {
        this.param5 = param5;
    }

    public String getParam6() {
        return param6;
    }

    public void setParam6(String param6) {
        this.param6 = param6;
    }

    public String getParam7() {
        return param7;
    }

    public void setParam7(String param7) {
        this.param7 = param7;
    }

    @Override
    public String toString() {
        String resultString = "";
        String typeString = "";
        switch (type){
            case STATISTIC_applicationActive:
                typeString = "STATISTIC_applicationActive";
                break;
            case STATISTIC_applicationPassive:
                typeString = "STATISTIC_applicationPassive";
                break;
            case STATISTIC_contentClosed:
                typeString = "STATISTIC_contentClosed";
                break;
            case STATISTIC_contentDeleted:
                typeString = "STATISTIC_contentDeleted";
                break;
            case STATISTIC_contentDownloaded:
                typeString = "STATISTIC_contentDownloaded";
                break;
            case STATISTIC_contentOpened:
                typeString = "STATISTIC_contentOpened";
                break;
            case STATISTIC_contentUpdated:
                typeString = "STATISTIC_contentUpdated";
                break;
            case STATISTIC_pageOpenedLandscape:
                typeString = "STATISTIC_pageOpenedLandscape";
                break;
            case STATISTIC_pageOpenedPortrait:
                typeString = "STATISTIC_pageOpenedPortrait";
                break;
            default:
                typeString = "unkonwn";

        }
        resultString =  "\nid: "+id;
        resultString += "\ntype:"+typeString;
        resultString += "\ncontent:"+contentId;
        resultString += "\nPage : "+page;
        resultString += "\ntime: "+time;
        resultString += "\nlat:"+(lat!=null ?lat.toString():"null");
        resultString += "\nlon:"+(lon!=null ?lon.toString():"null");
        resultString += "\n--------------------------------\n";

        return resultString;
    }
}
