package ak.detaysoft.galepress.search_models;

import java.util.ArrayList;

/**
 * Created by p1025 on 16.08.2016.
 */

public class SearchResult {

    private ArrayList<ContentSearchResult> contentSearchList;
    private ArrayList<TextSearchResult> textSearchList;

    public SearchResult(){
        contentSearchList = new ArrayList<ContentSearchResult>();
        textSearchList = new ArrayList<TextSearchResult>();
    }

    public ArrayList<ContentSearchResult> getContentSearchList() {
        return contentSearchList;
    }

    public void setContentSearchList(ArrayList<ContentSearchResult> contentSearchList) {
        this.contentSearchList = contentSearchList;
    }

    public ArrayList<TextSearchResult> getTextSearchList() {
        return textSearchList;
    }

    public void setTextSearchList(ArrayList<TextSearchResult> textSearchList) {
        this.textSearchList = textSearchList;
    }
}
