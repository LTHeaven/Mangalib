package es.heavensgat.mangalib.server.models;


import java.util.ArrayList;
import java.util.List;

public class Manga {
    private List<Chapter> chapters = new ArrayList<Chapter>();
    private String title;
    private String author;
    private String artist;
    private String summary;
    private String baseURL;
    private String coverImage;

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public List<Chapter> getChapters() {
        return chapters;
    }

    public void setChapters(List<Chapter> chapters) {
        this.chapters = chapters;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    @Override
    public String toString() {
        return "Manga{" +
                "chapters=" + chapters.size() +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", artist='" + artist + '\'' +
                ", summary='" + summary.substring(0, Math.min(summary.length(), 50)) + '\'' +
                ", baseURL='" + baseURL + '\'' +
                '}';
    }
}
