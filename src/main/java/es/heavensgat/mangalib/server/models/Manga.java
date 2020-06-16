package es.heavensgat.mangalib.server.models;



import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Manga")
public class Manga implements Comparable{
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "manga")
    private List<Chapter> chapters = new ArrayList<>();
    @Column(name = "title")
    private String title;
    @Column(name = "author")
    private String author;
    @Column(name = "artist")
    private String artist;
    @Column(name = "summary")
    private String summary;
    @Column(name = "baseURL")
    private String baseURL;
    @Column(name = "mangaFolderPath")
    private String mangaFolderPath;
    @Column(name = "added")
    private long added;
    @Column(name = "error")
    private boolean error = false;


    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public long getAdded() {
        return added;
    }

    public void setAdded(long added) {
        this.added = added;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public String getMangaFolderPath() {
        return mangaFolderPath;
    }

    public void setMangaFolderPath(String mangaFolderPath) {
        this.mangaFolderPath = mangaFolderPath;
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
    public int compareTo(Object o) {
        Manga manga= (Manga) o;
        return Long.compare(manga.getAdded(), added);
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
