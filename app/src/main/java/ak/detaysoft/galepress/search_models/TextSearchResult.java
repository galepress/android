package ak.detaysoft.galepress.search_models;

import java.util.ArrayList;

/**
 * Created by p1025 on 15.08.2016.
 */

public class TextSearchResult {

    private String contentId;
    private String contentTitle;
    private boolean isDownloaded;
    private ArrayList<FullTextSearchPageItem> pageItems;

    public TextSearchResult(){
        pageItems = new ArrayList<FullTextSearchPageItem>();
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public String getContentTitle() {
        return contentTitle;
    }

    public void setContentTitle(String contentTitle) {
        this.contentTitle = contentTitle;
    }

    public ArrayList<FullTextSearchPageItem> getPageItems() {
        return pageItems;
    }

    public void setPageItems(ArrayList<FullTextSearchPageItem> pageItems) {
        this.pageItems = pageItems;
    }

    public boolean isDownloaded() {
        return isDownloaded;
    }

    public void setDownloaded(boolean downloaded) {
        isDownloaded = downloaded;
    }
}
