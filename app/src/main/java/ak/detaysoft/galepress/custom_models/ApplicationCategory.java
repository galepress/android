package ak.detaysoft.galepress.custom_models;

/**
 * Created by p1025 on 10.11.2016.
 */

public class ApplicationCategory {

    private Integer id;
    private String coverImageUrl;

    public ApplicationCategory(){

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }
}
