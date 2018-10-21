package es.heavensgat.mangalib.server.models;

import java.util.List;

public class Chapter {
    private String title;
    private String firstPageURL;
    private List<Page> pages;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFirstPageURL() {
        return firstPageURL;
    }

    public void setFirstPageURL(String firstPageURL) {
        this.firstPageURL = firstPageURL;
    }

    public List<Page> getPages() {
        return pages;
    }

    public void setPages(List<Page> pages) {
        this.pages = pages;
    }
}
