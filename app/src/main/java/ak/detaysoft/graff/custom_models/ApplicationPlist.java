package ak.detaysoft.graff.custom_models;

/**
 * Created by p1025 on 03.04.2015.
 */
public class ApplicationPlist {

    private String key;
    private String value;

    public ApplicationPlist(String key, String value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
