package ak.detaysoft.galepress.search_models;

/**
 * Created by p1025 on 16.08.2016.
 */

public class FullTextSearchPageItem {

    private String text;
    private int page;
    private String udid;

    public FullTextSearchPageItem(){

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getUdid() {
        return udid;
    }

    public void setUdid(String udid) {
        this.udid = udid;
    }
}
