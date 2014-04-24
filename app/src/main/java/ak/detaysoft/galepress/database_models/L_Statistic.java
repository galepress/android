package ak.detaysoft.galepress.database_models;


import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.Date;

/**
 * Created by adem on 18/02/14.
 */
@DatabaseTable(tableName = "Statistic")
public class L_Statistic {

    @DatabaseField(generatedId = true) private Integer id;
    @DatabaseField private Integer contentId;
    @DatabaseField private Double lat;
    @DatabaseField private Double lon;
    @DatabaseField private Integer page;
    @DatabaseField private Date time;
    @DatabaseField private Integer type;
    @DatabaseField private String param5;
    @DatabaseField private String param6;
    @DatabaseField private String param7;

    public L_Statistic() {
    }

    public L_Statistic(Integer id, Integer contentId, Double lat, Double lon, Integer page, Date time, Integer type, String param5, String param6, String param7) {
        this.id = id;
        this.contentId = contentId;
        this.lat = lat;
        this.lon = lon;
        this.page = page;
        this.time = time;
        this.type = type;
        this.param5 = param5;
        this.param6 = param6;
        this.param7 = param7;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
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

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
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
}
