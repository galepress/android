package ak.detaysoft.galepress.service_models;
import org.json.JSONObject;


public class R_Category {

    private Integer id;
    private String name;


    public R_Category() {

    }

    public R_Category(JSONObject categoryObject) {
        this.id = categoryObject.optInt("id");
        this.name = categoryObject.optString("name");
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
