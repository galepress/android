package ak.detaysoft.graff.search_models;

/**
 * Created by p1025 on 16.08.2016.
 */

public class ReaderSearchResult {

    private String text;
    private int page;

    public ReaderSearchResult(){

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
}
