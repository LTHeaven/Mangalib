package es.heavensgat.mangalib.server.models;

/**
 * Created by bened_000 on 04.11.2018.
 */
public class MangaListing {
    private String mangaTitle;
    private String pdfPath;

    public MangaListing(String mangaTitle, String pdfPath) {
        this.mangaTitle = mangaTitle;
        this.pdfPath = pdfPath;
    }

    public String getMangaTitle() {
        return mangaTitle;
    }

    public void setMangaTitle(String mangaTitle) {
        this.mangaTitle = mangaTitle;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(String pdfPath) {
        this.pdfPath = pdfPath;
    }
}
