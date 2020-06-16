package es.heavensgat.mangalib.server.models;

import javax.persistence.*;

@Entity
@Table(name = "Page")
public class Page {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(cascade = CascadeType.ALL)
    private Chapter parentChapter;
    @Column(name = "page_number")
    private int pageNumber;
    @Enumerated(EnumType.STRING)
    private PageStatus status;
    @Column(name = "url")
    private String url;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chapter getParentChapter() {
        return parentChapter;
    }

    public void setParentChapter(Chapter parentChapter) {
        this.parentChapter = parentChapter;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public PageStatus getStatus() {
        return status;
    }

    public void setStatus(PageStatus status) {
        this.status = status;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Page{" +
                "parentChapter=" + parentChapter.getTitle() +
                ", pageNumber=" + pageNumber +
//                ", image=" + image +
                ", url='" + url + '\'' +
                '}';
    }
}
