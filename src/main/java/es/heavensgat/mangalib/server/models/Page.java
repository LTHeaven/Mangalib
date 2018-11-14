package es.heavensgat.mangalib.server.models;

public class Page {
    private Chapter parentChapter;
    private int pageNumber;
    private String imageFilePath;
//    private Image image;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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

    public String getImageFilePath() {
        return imageFilePath;
    }

    public void setImageFilePath(String imageFilePath) {
        this.imageFilePath = imageFilePath;
    }

    //    public Image getImage() {
//        return image;
//    }
//
//    public void setImage(Image image) {
//        this.image = image;
//    }

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
