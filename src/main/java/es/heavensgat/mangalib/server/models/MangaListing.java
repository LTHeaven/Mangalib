package es.heavensgat.mangalib.server.models;

/**
 * Created by bened_000 on 04.11.2018.
 */
public class MangaListing implements Comparable{
    private String mangaTitle;
    private String mangaSummary;
    private long added;
    private String pdfPath;
    private String status;

    public MangaListing(String mangaTitle) {
        this.mangaTitle = mangaTitle;
    }

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

    public String getMangaSummary() {
        return mangaSummary;
    }

    public void setMangaSummary(String mangaSummary) {
        this.mangaSummary = mangaSummary;
    }

    public long getAdded() {
        return added;
    }

    public void setAdded(long added) {
        this.added = added;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public int compareTo(Object o) {
        MangaListing mangaListing = (MangaListing) o;
        return Long.compare(mangaListing.getAdded(), added);
    }
}
