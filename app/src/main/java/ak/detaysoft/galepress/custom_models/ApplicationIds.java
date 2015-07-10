package ak.detaysoft.galepress.custom_models;

/**
 * Created by p1025 on 15.05.2015.
 */
public class ApplicationIds {

    private String name;
    private String id;

    public ApplicationIds(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setId(String id) {
        this.id = id;
    }
}
